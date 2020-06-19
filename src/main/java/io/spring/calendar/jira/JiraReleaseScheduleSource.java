/*
 * Copyright 2016-2020 the original author or authors.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.calendar.release.Release;
import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.ReleaseSchedule;
import io.spring.calendar.release.ReleaseScheduleSource;

import org.springframework.stereotype.Component;

/**
 * A {@link ReleaseScheduleSource} for projects managed in JIRA.
 *
 * @author Andy Wilkinson
 */
@Component
class JiraReleaseScheduleSource implements ReleaseScheduleSource {

	private final JiraOperations jiraOperations;

	private final JiraProjectFilter jiraProjectFilter;

	JiraReleaseScheduleSource(JiraOperations jiraOperations, JiraProjectFilter jiraProjectFilter) {
		this.jiraOperations = jiraOperations;
		this.jiraProjectFilter = jiraProjectFilter;
	}

	@Override
	public List<ReleaseSchedule> get() {
		return this.jiraOperations.getProjects().stream().filter(this.jiraProjectFilter)
				.map(this::createReleaseSchedule).collect(Collectors.toList());
	}

	private ReleaseSchedule createReleaseSchedule(JiraProject project) {
		List<Release> releases = this.jiraOperations.getVersions(project).stream().filter(this::hasReleaseDate)
				.map((version) -> createRelease(project, version)).collect(Collectors.toList());
		return new ReleaseSchedule(project.getName(), releases);
	}

	private boolean hasReleaseDate(JiraVersion version) {
		return version.getReleaseDate() != null;
	}

	private Release createRelease(JiraProject project, JiraVersion version) {

		try {
			return new Release(project.getName(), version.getName(), version.getReleaseDate(), getStatus(version),
					new URL(String.format("https://jira.spring.io/browse/%s/fixforversion/%s", project.getKey(),
							version.getId())));
		}
		catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Status getStatus(JiraVersion version) {
		return version.isReleased() ? Status.CLOSED : Status.OPEN;
	}

}
