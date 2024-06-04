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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.spring.calendar.github.Milestone.State;
import io.spring.calendar.github.Repository.Visibility;
import io.spring.calendar.release.Release;
import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.ReleaseSchedule;
import io.spring.calendar.release.ReleaseScheduleSource;

/**
 * A {@link ReleaseScheduleSource} for projects managed on GitHub.
 *
 * @author Andy Wilkinson
 */
class GitHubReleaseScheduleSource implements ReleaseScheduleSource {

	private final Map<String, Page<Milestone>> earlierMilestones = new HashMap<>();

	private final Map<String, Page<Repository>> earlierRepositories = new HashMap<>();

	private final List<String> organizations;

	private final GitHubOperations gitHub;

	GitHubReleaseScheduleSource(GitHubOperations gitHub, List<String> organizations) {
		this.gitHub = gitHub;
		this.organizations = organizations;
	}

	@Override
	public List<ReleaseSchedule> get() {
		return this.organizations.stream()
			.flatMap(this::getRepositories)
			.filter(this::include)
			.map(this::createReleaseSchedule)
			.toList();
	}

	private Stream<Repository> getRepositories(String organization) {
		Page<Repository> page = this.gitHub.getRepositories(organization, this.earlierRepositories.get(organization));
		this.earlierRepositories.put(organization, this.earlierRepositories.get(organization));
		return collectContent(page).stream();
	}

	private boolean include(Repository repository) {
		return (repository.getVisibility() == Visibility.PUBLIC) || repository.getName().endsWith("-commercial");
	}

	private ReleaseSchedule createReleaseSchedule(Repository repository) {
		Page<Milestone> page = this.gitHub.getMilestones(repository,
				this.earlierMilestones.get(repository.getFullName()));
		this.earlierMilestones.put(repository.getFullName(), page);
		List<Release> releases = getReleases(repository, page);
		return new ReleaseSchedule(repository.getDisplayName(), releases);
	}

	private List<Release> getReleases(Repository repository, Page<Milestone> page) {
		return collectContent(page).stream()
			.filter(this::hasReleaseDate)
			.map((Milestone milestone) -> createRelease(repository, milestone))
			.toList();
	}

	private <T> List<T> collectContent(Page<T> page) {
		List<T> content = new ArrayList<>();
		while (page != null) {
			content.addAll(page.getContent());
			page = page.next();
		}
		return content;
	}

	private boolean hasReleaseDate(Milestone milestone) {
		return milestone.getDueOn() != null;
	}

	private Release createRelease(Repository project, Milestone milestone) {
		try {
			return new Release(project.getDisplayName(), milestone.getTitle(),
					milestone.getDueOn()
						.withZoneSameInstant(ZoneId.of("Europe/London"))
						.format(DateTimeFormatter.ISO_LOCAL_DATE),
					getStatus(milestone), getUrl(project, milestone));
		}
		catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Status getStatus(Milestone milestone) {
		return (milestone.getState() == State.OPEN) ? Status.OPEN : Status.CLOSED;
	}

	private URL getUrl(Repository project, Milestone milestone) throws MalformedURLException {
		return (project.getVisibility() == Repository.Visibility.PUBLIC)
				? new URL(project.getHtmlUrl().toString() + "/milestone/" + milestone.getNumber()) : null;
	}

}
