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

package io.spring.calendar.release;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code ReleaseUpdater} updates the known {@link Release Releases}.
 *
 * @author Andy Wilkinson
 */
@Component
class ReleaseUpdater {

	private final List<Supplier<List<ProjectReleases>>> projectReleasesSuppliers;

	private final ReleaseRepository releaseRepository;

	ReleaseUpdater(List<Supplier<List<ProjectReleases>>> projectReleasesSuppliers,
			ReleaseRepository releaseRepository) {
		this.projectReleasesSuppliers = projectReleasesSuppliers;
		this.releaseRepository = releaseRepository;
	}

	@Transactional
	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void updateReleases() {
		this.projectReleasesSuppliers //
				.stream() //
				.map((supplier) -> {
					return supplier.get();
				}).flatMap((list) -> {
					return list.stream();
				}).forEach(this::updateReleases);
	}

	private void updateReleases(ProjectReleases projectReleases) {
		this.releaseRepository.deleteAllByProject(projectReleases.getProject());
		this.releaseRepository.save(projectReleases.getReleases());
	}

}
