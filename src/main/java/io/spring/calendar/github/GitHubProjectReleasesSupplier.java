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

import org.springframework.stereotype.Component;

import io.spring.calendar.release.ProjectReleases;
import io.spring.calendar.release.Release;

/**
 * A {@link Supplier} of {@link ProjectReleases} for {@link GitHubProject GitHubProjects}.
 *
 * @author Andy Wilkinson
 */
@Component
class GitHubProjectReleasesSupplier implements Supplier<List<ProjectReleases>> {

	private final Map<String, Page<Milestone>> earlierMilestones = new HashMap<String, Page<Milestone>>();

	private final GitHubProjectRepository repository;

	private final GitHubOperations gitHub;

	GitHubProjectReleasesSupplier(GitHubProjectRepository repository,
			GitHubOperations gitHub) {
		this.repository = repository;
		this.gitHub = gitHub;
	}

	@Override
	public List<ProjectReleases> get() {
		return this.repository.findAll().stream().map(this::createProjectReleases)
				.collect(Collectors.toList());
	}

	private ProjectReleases createProjectReleases(GitHubProject project) {
		String key = project.getOwner() + "/" + project.getRepo();
		Page<Milestone> page = this.gitHub.getMilestones(project.getOwner(),
				project.getRepo(), this.earlierMilestones.get(key));
		this.earlierMilestones.put(key, page);
		List<Release> releases = getReleases(project, page);
		return new ProjectReleases(project.getName(), releases);
	}

	private List<Release> getReleases(GitHubProject project, Page<Milestone> page) {
		return collectContent(page)//
				.stream() //
				.filter(this::hasReleaseDate) //
				.map((Milestone milestone) -> {
					return createRelease(project, milestone);
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

	private Release createRelease(GitHubProject project, Milestone milestone) {
		return new Release(project.getName(), milestone.getTitle(),
				milestone.getDueOn().withZoneSameInstant(ZoneId.of("Europe/London"))
						.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

}
