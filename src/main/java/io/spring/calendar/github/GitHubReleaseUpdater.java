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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.spring.calendar.release.Release;
import io.spring.calendar.release.ReleaseRepository;

/**
 * Updates releases for projects managed in GitHub.
 *
 * @author Andy Wilkinson
 */
@Component
class GitHubReleaseUpdater {

	private final Map<String, String> eTags = new HashMap<String, String>();

	private final GitHubOperations gitHub;

	private final GitHubProjectRepository projectRepository;

	private final ReleaseRepository releaseRepository;

	GitHubReleaseUpdater(GitHubOperations gitHub,
			GitHubProjectRepository projectRepository,
			ReleaseRepository releaseRepository) {
		this.gitHub = gitHub;
		this.projectRepository = projectRepository;
		this.releaseRepository = releaseRepository;
	}

	@Scheduled(fixedRate = 5000)
	@Transactional
	public void updateReleases() {
		this.projectRepository.findAll().forEach(this::updateReleases);
	}

	private void updateReleases(GitHubProject project) {
		String key = project.getOwner() + "/" + project.getRepo();
		Page<Milestone> page = this.gitHub.getMilestones(project.getOwner(),
				project.getRepo(), this.eTags.get(key));
		if (page != null) {
			this.eTags.put(key, page.getETag());
			updateReleases(project, page);
		}
	}

	private void updateReleases(GitHubProject project, Page<Milestone> page) {
		List<Release> releases = collectContent(page).stream().filter((milestone) -> {
			return milestone.getDueOn() != null;
		}).map((Milestone milestone) -> {
			return new Release(project.getName(), milestone.getTitle(),
					milestone.getDueOn().withZoneSameInstant(ZoneId.systemDefault())
							.toEpochSecond() * 1000);
		}).collect(Collectors.toList());
		this.releaseRepository.deleteAllByProject(project.getName());
		this.releaseRepository.save(releases);
	}

	private <T> List<T> collectContent(Page<T> page) {
		List<T> content = new ArrayList<T>();
		while (page != null) {
			content.addAll(page.getContent());
			page = page.next();
		}
		return content;
	}

}
