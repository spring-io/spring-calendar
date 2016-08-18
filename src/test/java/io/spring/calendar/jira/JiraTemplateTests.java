/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.calendar.jira;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import io.spring.calendar.test.TestMethodResponseCreator;
import io.spring.calendar.test.TestMethodResponseTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

/**
 * Tests for {@link JiraTemplate}.
 *
 * @author Andy Wilkinson
 */
@RestClientTest(components = JiraTemplate.class)
@RunWith(SpringRunner.class)
@TestExecutionListeners(mergeMode = MergeMode.MERGE_WITH_DEFAULTS, listeners = TestMethodResponseTestExecutionListener.class)
public class JiraTemplateTests {

	@Autowired
	private JiraTemplate jira;

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private TestMethodResponseCreator testMethodResponse;

	@Test
	public void getProjects() {
		this.server.expect(requestTo("https://jira.spring.io/rest/api/2/project"))
				.andRespond(this.testMethodResponse);
		List<JiraProject> projects = this.jira.getProjects();
		assertThat(projects).hasSize(92);
		JiraProject project = projects.get(0);
		assertThat(project.getName()).isEqualTo("Greenhouse");
		assertThat(project.getUri())
				.isEqualTo(URI.create("https://jira.spring.io/rest/api/2/project/10540"));
	}

	@Test
	public void getVersions() {
		this.server
				.expect(requestTo(
						"https://jira.spring.io/rest/api/2/project/SPR/versions"))
				.andRespond(this.testMethodResponse);
		List<JiraVersion> versions = this.jira.getVersions(new JiraProject("Spring Framework",
				URI.create("https://jira.spring.io/rest/api/2/project/SPR")));
		assertThat(versions).hasSize(161);
		JiraVersion version = versions.get(40);
		assertThat(version.isReleased()).isEqualTo(true);
		assertThat(version.getName()).isEqualTo("2.0.4");
		assertThat(version.getReleaseDate()).isEqualTo("2007-04-09");
	}

}
