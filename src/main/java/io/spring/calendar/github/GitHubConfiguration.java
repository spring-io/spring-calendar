/*
 * Copyright 2016-2019 the original author or authors.
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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for GitHub-related functionality.
 *
 * @author Andy Wilkinson
 */
@Configuration
@EnableConfigurationProperties(GitHubProperties.class)
class GitHubConfiguration {

	private final GitHubProperties gitHubProperties;

	GitHubConfiguration(GitHubProperties gitHubProperties) {
		this.gitHubProperties = gitHubProperties;
	}

	@Bean
	GitHubOperations gitHubOperations(RestTemplateBuilder restTemplateBuilder) {
		return new GitHubTemplate(this.gitHubProperties.getUsername(),
				this.gitHubProperties.getPassword(), new RegexLinkParser(),
				restTemplateBuilder);
	}

	@Bean
	GitHubProjectReleasesSupplier releasesSupplier(GitHubOperations gitHubOperations) {
		return new GitHubProjectReleasesSupplier(gitHubOperations,
				this.gitHubProperties.getOrganizations());
	}

}
