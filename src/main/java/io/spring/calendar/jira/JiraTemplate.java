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

package io.spring.calendar.jira;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Standard implementation of {@code JiraOperations}.
 *
 * @author Andy Wilkinson
 */
@Component
class JiraTemplate implements JiraOperations {

	private final RestTemplate restTemplate;

	JiraTemplate(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Override
	public List<JiraProject> getProjects() {
		ResponseEntity<JiraProject[]> entity = this.restTemplate
				.getForEntity("https://jira.spring.io/rest/api/2/project", JiraProject[].class);
		return Arrays.asList(entity.getBody());
	}

	@Override
	public List<JiraVersion> getVersions(JiraProject project) {
		ResponseEntity<JiraVersion[]> entity;
		entity = this.restTemplate.getForEntity(project.getUri().toString() + "/versions", JiraVersion[].class);
		return Arrays.asList(entity.getBody());
	}

}
