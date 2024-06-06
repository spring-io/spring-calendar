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

import java.io.StringWriter;
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

	private static final String COMMERCIAL_REPOSITORY_NAME_SUFFIX = "-commercial";

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
		String projectName = getProjectName(repository);
		List<Release> releases = getReleases(projectName, repository, page);
		return new ReleaseSchedule(projectName, releases);
	}

	private String getProjectName(Repository repository) {
		String name = repository.getName();
		if (name.endsWith(GitHubReleaseScheduleSource.COMMERCIAL_REPOSITORY_NAME_SUFFIX)) {
			name = name.substring(0,
					name.length() - GitHubReleaseScheduleSource.COMMERCIAL_REPOSITORY_NAME_SUFFIX.length());
		}
		return capitalize(name.replace('-', ' '));
	}

	private static String capitalize(String input) {
		StringWriter output = new StringWriter();
		for (int i = 0; i < input.length(); i++) {
			if (i == 0 || i > 0 && input.charAt(i - 1) == ' ') {
				output.append(Character.toUpperCase(input.charAt(i)));
			}
			else {
				output.append(input.charAt(i));
			}
		}
		return output.toString();
	}

	private List<Release> getReleases(String projectName, Repository repository, Page<Milestone> page) {
		return collectContent(page).stream()
			.filter(this::hasReleaseDate)
			.map((Milestone milestone) -> createRelease(projectName, repository, milestone))
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

	private Release createRelease(String projectName, Repository repository, Milestone milestone) {
		return new Release(projectName, milestone.getTitle(),
				milestone.getDueOn()
					.withZoneSameInstant(ZoneId.of("Europe/London"))
					.format(DateTimeFormatter.ISO_LOCAL_DATE),
				getStatus(milestone), getUrl(repository, milestone),
				repository.getName().endsWith(GitHubReleaseScheduleSource.COMMERCIAL_REPOSITORY_NAME_SUFFIX));
	}

	private Status getStatus(Milestone milestone) {
		return (milestone.getState() == State.OPEN) ? Status.OPEN : Status.CLOSED;
	}

	private URL getUrl(Repository repository, Milestone milestone) {
		try {
			if (repository.getName().endsWith(COMMERCIAL_REPOSITORY_NAME_SUFFIX)) {
				String url = "https://enterprise.spring.io/projects/" + repository.getName();
				if (milestone.getState() == State.CLOSED) {
					url = url + "/changelog/" + milestone.getTitle();
				}
				return new URL(url);
			}
			return new URL(repository.getHtmlUrl().toString() + "/milestone/" + milestone.getNumber());
		}
		catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
