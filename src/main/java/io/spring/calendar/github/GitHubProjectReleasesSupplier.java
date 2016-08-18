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

package io.spring.calendar.github;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.calendar.github.Milestone.State;
import io.spring.calendar.release.Project;
import io.spring.calendar.release.ProjectReleases;
import io.spring.calendar.release.Release;
import io.spring.calendar.release.Release.Status;

/**
 * A {@link Supplier} of {@link ProjectReleases} for projects managed on GitHub.
 *
 * @author Andy Wilkinson
 */
class GitHubProjectReleasesSupplier implements Supplier<List<ProjectReleases>> {

	private final Map<String, Page<Milestone>> earlierMilestones = new HashMap<String, Page<Milestone>>();

	private final Map<String, Page<Repository>> earlierRepositories = new HashMap<String, Page<Repository>>();

	private final List<String> organizations;

	private final GitHubOperations gitHub;

	GitHubProjectReleasesSupplier(GitHubOperations gitHub, List<String> organizations) {
		this.gitHub = gitHub;
		this.organizations = organizations;
	}

	@Override
	public List<ProjectReleases> get() {
		return this.organizations //
				.stream() //
				.flatMap(this::getRepositories) //
				.map(this::createProjectReleases) //
				.collect(Collectors.toList());
	}

	private Stream<Repository> getRepositories(String organization) {
		Page<Repository> page = this.gitHub.getPublicRepositories(organization,
				this.earlierRepositories.get(organization));
		this.earlierRepositories.put(organization,
				this.earlierRepositories.get(organization));
		return collectContent(page).stream();
	}

	private ProjectReleases createProjectReleases(Repository repository) {
		Page<Milestone> page = this.gitHub.getMilestones(repository,
				this.earlierMilestones.get(repository.getMilestonesUrl()));
		this.earlierMilestones.put(repository.getFullName(), page);
		List<Release> releases = getReleases(repository, page);
		return new ProjectReleases(repository.getDisplayName(), releases);
	}

	private List<Release> getReleases(Repository repository, Page<Milestone> page) {
		return collectContent(page) //
				.stream() //
				.filter(this::hasReleaseDate) //
				.map((Milestone milestone) -> {
					return createRelease(repository, milestone);
				}) //
				.collect(Collectors.toList());
	}

	private <T> List<T> collectContent(Page<T> page) {
		List<T> content = new ArrayList<T>();
		while (page != null) {
			content.addAll(page.getContent());
			page = page.next();
		}
		return content;
	}

	private boolean hasReleaseDate(Milestone milestone) {
		return milestone.getDueOn() != null;
	}

	private Release createRelease(Repository repository, Milestone milestone) {
		return new Release(
				new Project(repository.getDisplayName(), repository.getHtmlUrl()),
				milestone.getTitle(),
				milestone.getDueOn().withZoneSameInstant(ZoneId.of("Europe/London"))
						.format(DateTimeFormatter.ISO_LOCAL_DATE), getStatus(milestone));
	}

	private Status getStatus(Milestone milestone) {
		return milestone.getState() == State.open ? Status.OPEN : Status.CLOSED;
	}

}
