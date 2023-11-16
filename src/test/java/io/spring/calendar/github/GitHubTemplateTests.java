/*
 * Copyright 2016-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.calendar.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.calendar.github.GitHubTemplateTests.TemplateConfiguration;
import io.spring.calendar.test.TestMethodResponseCreator;
import io.spring.calendar.test.TestMethodResponseTestExecutionListener;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.util.CollectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GitHubTemplate}.
 *
 * @author Andy Wilkinson
 */
@RestClientTest
@TestExecutionListeners(mergeMode = MergeMode.MERGE_WITH_DEFAULTS,
		listeners = TestMethodResponseTestExecutionListener.class)
@ContextConfiguration(classes = TemplateConfiguration.class)
class GitHubTemplateTests {

	private final Repository repository = new Repository("spring-boot", "spring-projects/spring-boot",
			"https://api.github.com/repos/spring-projects/spring-boot/milestones",
			"https://github.com/spring-projects/spring-boot");

	@Autowired
	private GitHubTemplate gitHub;

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private TestMethodResponseCreator testMethodResponse;

	@Autowired
	RestTemplateBuilder restTemplateBuilder;

	@Test
	void getMilestones() throws MalformedURLException {
		this.server
			.expect(requestTo(
					"https://api.github.com/repos/spring-projects/spring-boot/milestones?state=all&per_page=100"))
			.andRespond(this.testMethodResponse);
		Page<Milestone> page = this.gitHub.getMilestones(this.repository, null);
		assertThat(page.getContent()).hasSize(68);
		this.server.verify();
		assertThat(page.getContent().get(67).getState()).isEqualTo(Milestone.State.OPEN);
		assertThat(page.getContent().get(67).getNumber()).isEqualTo(52);
	}

	@Test
	void getPublicRepositories() throws URISyntaxException, MalformedURLException {
		this.server.expect(requestTo("https://api.github.com/orgs/spring-projects/repos?type=public&per_page=100"))
			.andRespond(this.testMethodResponse);
		Page<Repository> page = this.gitHub.getPublicRepositories("spring-projects", null);
		assertThat(page.getContent()).hasSize(30);
		assertThat(page.getContent().get(0).getMilestonesUrl())
			.isEqualTo(new URL("https://api.github.com/repos/spring-projects/Spring-Integration-in-Action/milestones"));
		assertThat(page.getContent().get(0).getHtmlUrl())
			.isEqualTo(new URL("https://github.com/spring-projects/Spring-Integration-in-Action"));
		this.server.verify();
	}

	@Test
	void paging() {
		this.server
			.expect(requestTo(
					"https://api.github.com/repos/spring-projects/spring-boot/milestones?state=all&per_page=100"))
			.andRespond(response(2, 3));
		this.server.expect(requestTo(createUrl(2))).andRespond(response(3, 3));
		this.server.expect(requestTo(createUrl(3)))
			.andRespond(withSuccess().body("[]").contentType(MediaType.APPLICATION_JSON));
		Page<Milestone> page = this.gitHub.getMilestones(this.repository, null);
		page.next().next();
		this.server.verify();
	}

	@Test
	void conditionalRequests() {
		String originalUrl = "https://api.github.com/repos/spring-projects/spring-boot/milestones?state=all&per_page=100";
		this.server.expect(requestTo(originalUrl)).andRespond(response(2, 3, "\"abc\""));
		this.server.expect(requestTo(createUrl(2))).andRespond(response(3, 3, "\"bcd\""));
		this.server.expect(requestTo(createUrl(3))).andRespond(response("\"cde\""));
		this.server.expect(requestTo(originalUrl))
			.andExpect(header(HttpHeaders.IF_NONE_MATCH, "\"abc\""))
			.andRespond(withStatus(HttpStatus.NOT_MODIFIED));
		this.server.expect(requestTo(createUrl(2)))
			.andExpect(header(HttpHeaders.IF_NONE_MATCH, "\"bcd\""))
			.andRespond(withStatus(HttpStatus.NOT_MODIFIED));
		this.server.expect(requestTo(createUrl(3)))
			.andExpect(header(HttpHeaders.IF_NONE_MATCH, "\"cde\""))
			.andRespond(withStatus(HttpStatus.NOT_MODIFIED));
		Page<Milestone> firstPage = this.gitHub.getMilestones(this.repository, null);
		firstPage.next().next();
		this.gitHub.getMilestones(this.repository, firstPage).next().next();
		this.server.verify();
	}

	@Test
	void requestIsNotConditionalWhenEarlierContentFilledThePage() throws JsonProcessingException {
		String originalUrl = "https://api.github.com/repos/spring-projects/spring-boot/milestones?state=all&per_page=100";
		this.server.expect(requestTo(originalUrl))
			.andRespond(withSuccess().body(new ObjectMapper().writeValueAsString(createMilestones(100)))
				.contentType(MediaType.APPLICATION_JSON));
		this.server.expect(requestTo(originalUrl))
			.andExpect(missingHeader("If-None-Match"))
			.andRespond(withSuccess().body(new ObjectMapper().writeValueAsString(createMilestones(100)))
				.contentType(MediaType.APPLICATION_JSON));
		Page<Milestone> firstPage = this.gitHub.getMilestones(this.repository, null);
		this.gitHub.getMilestones(this.repository, firstPage);
		this.server.verify();
	}

	private List<Map<String, String>> createMilestones(int count) {
		List<Map<String, String>> milestones = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Map<String, String> milestone = new HashMap<>();
			milestones.add(milestone);
		}
		return milestones;
	}

	private ResponseCreator response(int nextPage, int lastPage) {
		return response(nextPage, lastPage, null);
	}

	private ResponseCreator response(int nextPage, int lastPage, String etag) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Link", createLinkHeader(nextPage, lastPage));
		if (etag != null) {
			headers.setETag(etag);
		}
		return withSuccess().body("[]").headers(headers).contentType(MediaType.APPLICATION_JSON);
	}

	private ResponseCreator response(String etag) {
		HttpHeaders headers = new HttpHeaders();
		if (etag != null) {
			headers.setETag(etag);
		}
		return withSuccess().body("[]").headers(headers).contentType(MediaType.APPLICATION_JSON);
	}

	private String createUrl(int page) {
		return String.format("https://api.github.com/repositories/6296790/milestones?state=all&per_page=10&page=%d",
				page);
	}

	private String createLinkHeader(int nextPage, int lastPage) {
		return String.format("%s, %s", createLink("next", nextPage), createLink("last", lastPage));
	}

	private String createLink(String rel, int page) {
		return String.format("<%s>; rel=\"%s\"", createUrl(page), rel);
	}

	private RequestMatcher missingHeader(final String headerName) {
		return new RequestMatcher() {
			@Override
			public void match(ClientHttpRequest request) throws IOException, AssertionError {
				List<String> header = request.getHeaders().get(headerName);
				if (!CollectionUtils.isEmpty(header)) {
					throw new AssertionError("Found " + headerName + " header with value " + header);
				}
			}
		};
	}

	/**
	 * Test configuration for {@link GitHubTemplate}.
	 */
	@Configuration
	static class TemplateConfiguration {

		@Bean
		GitHubTemplate gitHubTemplate(RestTemplateBuilder restTemplateBuilder) {
			return new GitHubTemplate("user", "secret", new RegexLinkParser(), restTemplateBuilder);
		}

	}

}
