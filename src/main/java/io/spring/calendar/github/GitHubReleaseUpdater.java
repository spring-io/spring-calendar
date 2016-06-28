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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import io.spring.calendar.release.Release;
import io.spring.calendar.release.ReleaseRepository;

/**
 * Updates releases for projects managed in GitHub.
 *
 * @author Andy Wilkinson
 */
@Component
class GitHubReleaseUpdater {

	private final GitHubProjectRepository orojectRepository;

	private final ReleaseRepository releaseRepository;

	GitHubReleaseUpdater(GitHubProjectRepository orojectRepository,
			ReleaseRepository releaseRepository) {
		this.orojectRepository = orojectRepository;
		this.releaseRepository = releaseRepository;
	}

	@Scheduled(fixedRate = 10000)
	@Transactional
	public void updateReleases() {
		this.orojectRepository.findAll().forEach(this::updateReleases);
	}

	private void updateReleases(GitHubProject project) {
		ResponseEntity<Milestone[]> entity = new RestTemplate()
				.getForEntity("https://api.github.com/repos/" + project.getOwner() + "/"
						+ project.getRepo() + "/milestones", Milestone[].class);
		List<Release> releases = Arrays.asList(entity.getBody()).stream()
				.filter((milestone) -> {
					return milestone.getDueOn() != null;
				}).map((Milestone milestone) -> {
					return new Release(project.getName(), milestone.getTitle(),
							milestone.getDueOn().toEpochSecond() * 1000);
				}).collect(Collectors.toList());
		this.releaseRepository.deleteAllByProject(project.getName());
		this.releaseRepository.save(releases);
	}

}
