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

import java.text.SimpleDateFormat;
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
 * Updates releases for projects managed in Jira.
 *
 * @author Andy Wilkinson
 */
@Component
class JiraReleaseUpdater {

	private final JiraProjectRepository projectRepository;

	private final ReleaseRepository releaseRepository;

	JiraReleaseUpdater(JiraProjectRepository projectRepository,
			ReleaseRepository releaseRepository) {
		this.projectRepository = projectRepository;
		this.releaseRepository = releaseRepository;
	}

	@Scheduled(fixedRate = 60 * 60 * 1000)
	@Transactional
	public void updateReleases() {
		this.projectRepository.findAll().forEach(this::updateReleases);
	}

	private void updateReleases(JiraProject project) {
		ResponseEntity<Version[]> entity = new RestTemplate()
				.getForEntity("https://jira.spring.io/rest/api/2/project/"
						+ project.getKey() + "/versions", Version[].class);
		List<Release> releases = Arrays.asList(entity.getBody()).stream()
				.filter((version) -> {
					return version.getReleaseDate() != null;
				}).map((Version version) -> {
					try {
						return new Release(project.getName(), version.getName(),
								new SimpleDateFormat("yyyy-MM-dd")
										.parse(version.getReleaseDate()).getTime());
					}
					catch (Exception ex) {
						throw new IllegalStateException(
								"Failed to parse " + version.getReleaseDate());
					}
				}).collect(Collectors.toList());
		this.releaseRepository.deleteAllByProject(project.getName());
		this.releaseRepository.save(releases);
	}

}
