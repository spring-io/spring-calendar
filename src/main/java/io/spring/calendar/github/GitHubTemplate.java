/*
 * Copyright 2016-2024 the original author or authors.
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
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
class GitHubTemplate implements GitHubOperations {

	private final RestOperations rest;

	private final LinkParser linkParser;

	/**
	 * Creates a new {@code GitHubTemplate} that will use the given {@code token} to
	 * authenticate, and the given {@code linkParser} to parse links from responses'
	 * {@code Link} header. It will use a {@link RestTemplate} created from the given
	 * {@code restTemplateBuilder}.
	 * @param token the token
	 * @param linkParser the link parser
	 * @param restTemplateBuilder the builder
	 */
	GitHubTemplate(String token, LinkParser linkParser, RestTemplateBuilder restTemplateBuilder) {
		if (StringUtils.hasText(token)) {
			restTemplateBuilder = restTemplateBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}
		this.rest = restTemplateBuilder.additionalCustomizers((restTemplate) -> {
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				@Override
				public void handleError(ClientHttpResponse response) throws IOException {
					if (response.getStatusCode() == HttpStatus.FORBIDDEN
							&& response.getHeaders().getFirst("X-RateLimit-Remaining").equals("0")) {
						throw new IllegalStateException("Rate limit exceeded. Limit will reset at "
								+ new Date(Long.valueOf(response.getHeaders().getFirst("X-RateLimit-Reset")) * 1000));
					}
				}
			});
		}).build();
		this.linkParser = linkParser;
	}

	@Override
	public Page<Milestone> getMilestones(Repository repository, Page<Milestone> earlierResponse) {
		String url = repository.getMilestonesUrl().toString() + "?state=all&per_page=100";
		return new PageSupplier<>(url, earlierResponse, Milestone[].class).get();
	}

	@Override
	public Page<Repository> getRepositories(String organization, Page<Repository> earlierResponse) {
		String url = (earlierResponse != null) ? earlierResponse.getUrl()
				: "https://api.github.com/orgs/" + organization + "/repos?per_page=100";
		return new PageSupplier<>(url, earlierResponse, Repository[].class).get();
	}

	private class PageSupplier<T> implements Supplier<Page<T>> {

		private String url;

		private Page<T> page;

		private Page<T> earlierResponse;

		private Class<T[]> type;

		PageSupplier(String url, Page<T> earlierResponse, Class<T[]> type) {
			this.url = url;
			this.earlierResponse = earlierResponse;
			this.type = type;
		}

		@Override
		public Page<T> get() {
			if (this.page == null) {
				this.page = getPage(this.url, this.type);
			}
			return this.page;
		}

		private Page<T> getPage(String url, Class<T[]> type) {
			if (!StringUtils.hasText(url)) {
				return null;
			}
			HttpHeaders headers = new HttpHeaders();
			if (this.earlierResponse != null
					&& (this.earlierResponse.next() != null || this.earlierResponse.getContent().size() != 100)) {
				headers.setIfNoneMatch(this.earlierResponse.getEtag());
			}

			ResponseEntity<T[]> response = GitHubTemplate.this.rest
				.exchange(new RequestEntity<>(headers, HttpMethod.GET, URI.create(this.url)), this.type);
			if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
				Page<T> nextEarlierResponse = this.earlierResponse.next();
				return new StandardPage<>(this.earlierResponse.getContent(), this.url, this.earlierResponse.getEtag(),
						new PageSupplier<>((nextEarlierResponse != null) ? nextEarlierResponse.getUrl() : null,
								nextEarlierResponse, this.type));
			}
			else {
				return new StandardPage<>(Arrays.asList(response.getBody()), this.url, response.getHeaders().getETag(),
						new PageSupplier<>(getNextUrl(response), null, this.type));
			}

		}

		private String getNextUrl(ResponseEntity<?> response) {
			return GitHubTemplate.this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
		}

	}

}
