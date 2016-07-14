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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.spring.calendar.release.ProjectReleases;
import io.spring.calendar.release.Release;

/**
 * A {@link Supplier} of {@link ProjectReleases} for projects managed in JIRA.
 *
 * @author Andy Wilkinson
 */
@Component
class JiraProjectReleasesSupplier implements Supplier<List<ProjectReleases>> {

	private final JiraOperations jiraOperations;

	private final JiraProjectFilter jiraProjectFilter;

	JiraProjectReleasesSupplier(JiraOperations jiraOperations,
			JiraProjectFilter jiraProjectFilter) {
		this.jiraOperations = jiraOperations;
		this.jiraProjectFilter = jiraProjectFilter;
	}

	@Override
	public List<ProjectReleases> get() {
		return this.jiraOperations.getProjects() //
				.stream() //
				.filter(this.jiraProjectFilter) //
				.map(this::createProjectReleases) //
				.collect(Collectors.toList());
	}

	private ProjectReleases createProjectReleases(JiraProject project) {
		List<Release> releases = this.jiraOperations.getVersions(project).stream() //
				.filter(this::hasReleaseDate) //
				.map((version) -> {
					return createRelease(project, version);
				}) //
				.collect(Collectors.toList());
		return new ProjectReleases(project.getName(), releases);
	}

	private boolean hasReleaseDate(JiraVersion version) {
		return version.getReleaseDate() != null;
	}

	private Release createRelease(JiraProject project, JiraVersion version) {
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
