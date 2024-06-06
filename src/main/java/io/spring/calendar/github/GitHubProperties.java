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

import java.util.Collections;
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

	private final List<Organization> organizations;

	GitHubProperties(String token, List<Organization> organizations) {
		this.token = token;
		this.organizations = organizations;
	}

	String getToken() {
		return this.token;
	}

	List<Organization> getOrganizations() {
		return this.organizations;
	}

	static class Organization {

		private final String name;

		private final List<Transform> transforms;

		Organization(String name, List<Transform> transforms) {
			this.name = name;
			this.transforms = (transforms != null) ? transforms : Collections.emptyList();
		}

		String getName() {
			return this.name;
		}

		List<Transform> getTransforms() {
			return this.transforms;
		}

	}

	static class Transform {

		private final String repository;

		private final String displayName;

		private final String commercialProjectId;

		Transform(String repository, String displayName, String commercialProjectId) {
			this.repository = repository;
			this.displayName = displayName;
			this.commercialProjectId = commercialProjectId;
		}

		String getRepository() {
			return this.repository;
		}

		String getDisplayName() {
			return this.displayName;
		}

		String getCommercialProjectId() {
			return this.commercialProjectId;
		}

	}

}
