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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for accessing GitHub.
 *
 * @author Andy Wilkinson
 */
@ConfigurationProperties("calendar.github")
class GitHubProperties {

	private final String token;

	private final List<String> organizations;

	GitHubProperties(String token, List<String> organizations) {
		this.token = token;
		this.organizations = organizations;
	}

	String getToken() {
		return this.token;
	}

	List<String> getOrganizations() {
		return this.organizations;
	}

}
