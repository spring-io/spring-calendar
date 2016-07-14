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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.spring.calendar.release.ProjectReleases;
import io.spring.calendar.release.Release;

/**
 * A {@link Supplier} of {@link ProjectReleases} for {@link JiraProject JiraProjects}.
 *
 * @author Andy Wilkinson
 */
class JiraProjectReleasesSupplier implements Supplier<List<ProjectReleases>> {

	private final JiraProjectRepository repository;

	JiraProjectReleasesSupplier(JiraProjectRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<ProjectReleases> get() {
		return this.repository.findAll().stream().map(this::createProjectReleases)
				.collect(Collectors.toList());
	}

	private ProjectReleases createProjectReleases(JiraProject project) {
		List<Release> releases = getVersions(project)//
				.filter(this::hasReleaseDate) //
				.map((version) -> {
					return createRelease(project, version);
				}) //
				.collect(Collectors.toList());
		return new ProjectReleases(project.getName(), releases);
	}

	private Stream<Version> getVersions(JiraProject project) {
		ResponseEntity<Version[]> entity = new RestTemplate()
				.getForEntity("https://jira.spring.io/rest/api/2/project/"
						+ project.getKey() + "/versions", Version[].class);
		return Arrays.asList(entity.getBody()).stream();
	}

	private boolean hasReleaseDate(Version version) {
		return version.getReleaseDate() != null;
	}

	private Release createRelease(JiraProject project, Version version) {
		try {
			return new Release(project.getName(), version.getName(),
					version.getReleaseDate());
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to parse " + version.getReleaseDate());
		}
	}

}
