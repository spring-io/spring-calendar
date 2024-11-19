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

import io.spring.calendar.github.GitHubProperties.Organization;
import io.spring.calendar.github.GitHubProperties.Transform;
import io.spring.calendar.github.Milestone.State;
import io.spring.calendar.github.Repository.Visibility;
import io.spring.calendar.release.Release;
import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.Release.Type;
import io.spring.calendar.release.ReleaseSchedule;
import io.spring.calendar.release.ReleaseScheduleSource;

import org.springframework.util.Assert;

/**
 * A {@link ReleaseScheduleSource} for projects managed on GitHub.
 *
 * @author Andy Wilkinson
 */
class GitHubReleaseScheduleSource implements ReleaseScheduleSource {

	private final Map<String, Page<Milestone>> earlierMilestones = new HashMap<>();

	private final Map<String, Page<Repository>> earlierRepositories = new HashMap<>();

	private final List<Organization> organizations;

	private final GitHubOperations gitHub;

	GitHubReleaseScheduleSource(GitHubOperations gitHub, List<Organization> organizations) {
		this.gitHub = gitHub;
		this.organizations = organizations;
	}

	@Override
	public List<ReleaseSchedule> get() {
		return this.organizations.stream()
			.flatMap(this::getRepositories)
			.filter(Project::include)
			.map(this::createReleaseSchedule)
			.toList();
	}

	private Stream<Project> getRepositories(Organization organization) {
		String organizationName = organization.getName();
		Map<String, Transform> transforms = new HashMap<>();
		for (Transform transform : organization.getTransforms()) {
			Transform existing = transforms.put(transform.getRepository(), transform);
			Assert.isNull(existing, () -> "Found duplicate transform for %s/%s".formatted(organization.getName(),
					transform.getRepository()));
		}
		Page<Repository> page = this.gitHub.getRepositories(organizationName,
				this.earlierRepositories.get(organizationName));
		this.earlierRepositories.put(organizationName, this.earlierRepositories.get(organizationName));
		return collectContent(page).stream()
			.map((repository) -> asProject(repository, transforms.get(repository.getName())));
	}

	private Project asProject(Repository repository, Transform transform) {
		return (transform != null) ? Project.from(repository, transform) : Project.from(repository);
	}

	private ReleaseSchedule createReleaseSchedule(Project project) {
		Page<Milestone> page = this.gitHub.getMilestones(project.getRepository(),
				this.earlierMilestones.get(project.getRepository().getFullName()));
		this.earlierMilestones.put(project.getRepository().getFullName(), page);
		List<Release> releases = getReleases(project, page);
		return new ReleaseSchedule(project.getName(), releases);
	}

	private List<Release> getReleases(Project project, Page<Milestone> page) {
		return collectContent(page).stream()
			.filter(this::hasReleaseDate)
			.map((Milestone milestone) -> createRelease(project, milestone))
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

	private Release createRelease(Project project, Milestone milestone) {
		return new Release(project.getName(), milestone.getTitle(),
				milestone.getDueOn()
					.withZoneSameInstant(ZoneId.of("Europe/London"))
					.format(DateTimeFormatter.ISO_LOCAL_DATE),
				getStatus(milestone), project.urlFor(milestone), getType(project));
	}

	private Status getStatus(Milestone milestone) {
		return (milestone.getState() == State.OPEN) ? Status.OPEN : Status.CLOSED;
	}

	private Type getType(Project project) {
		return project.isCommercial() ? Type.ENTERPRISE : Type.OSS;
	}

	private static final class Project {

		private static final String COMMERCIAL_REPOSITORY_NAME_SUFFIX = "-commercial";

		private final Repository repository;

		private final String name;

		private final String commercialProjectId;

		private Project(Repository repository, String name, String commercialProjectId) {
			this.repository = repository;
			this.name = name;
			this.commercialProjectId = commercialProjectId;
		}

		private Repository getRepository() {
			return this.repository;
		}

		private boolean include() {
			return (this.repository.getVisibility() == Visibility.PUBLIC) || this.isCommercial();
		}

		private String getName() {
			return this.name;
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

		private boolean isCommercial() {
			return this.repository.getName().endsWith(COMMERCIAL_REPOSITORY_NAME_SUFFIX);
		}

		private URL urlFor(Milestone milestone) {
			try {
				if (this.repository.getName().endsWith(COMMERCIAL_REPOSITORY_NAME_SUFFIX)) {
					String url = "https://enterprise.spring.io/projects/" + this.commercialProjectId;
					if (milestone.getState() == State.CLOSED) {
						url = url + "/changelog/" + milestone.getTitle();
					}
					return new URL(url);
				}
				return new URL(this.repository.getHtmlUrl().toString() + "/milestone/" + milestone.getNumber());
			}
			catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
		}

		static Project from(Repository repository) {
			return from(repository, null);
		}

		static Project from(Repository repository, Transform transform) {
			return new Project(repository, getName(repository, transform),
					getCommercialProjectId(repository, transform));
		}

		private static String getName(Repository repository, Transform transform) {
			if (transform != null) {
				String name = transform.getDisplayName();
				if (name != null) {
					return name;
				}
			}
			return getName(repository);
		}

		private static String getName(Repository repository) {
			String name = repository.getName();
			if (name.endsWith(COMMERCIAL_REPOSITORY_NAME_SUFFIX)) {
				name = name.substring(0, name.length() - COMMERCIAL_REPOSITORY_NAME_SUFFIX.length());
			}
			return capitalize(name.replace('-', ' '));
		}

		private static String getCommercialProjectId(Repository repository, Transform transform) {
			if (transform != null) {
				String id = transform.getCommercialProjectId();
				if (id != null) {
					return id;
				}
			}
			return getCommercialProjectId(repository);
		}

		private static String getCommercialProjectId(Repository repository) {
			String id = repository.getName();
			if (id.endsWith(COMMERCIAL_REPOSITORY_NAME_SUFFIX)) {
				return id.substring(0, id.length() - COMMERCIAL_REPOSITORY_NAME_SUFFIX.length());
			}
			return null;
		}

	}

}
