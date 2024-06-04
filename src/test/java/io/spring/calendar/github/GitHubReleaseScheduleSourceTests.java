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

import java.time.ZonedDateTime;
import java.util.List;

import io.spring.calendar.github.Milestone.State;
import io.spring.calendar.github.Repository.Visibility;
import io.spring.calendar.release.ReleaseSchedule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link GitHubReleaseScheduleSource}.
 *
 * @author Andy Wilkinson
 */
class GitHubReleaseScheduleSourceTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final GitHubReleaseScheduleSource source = new GitHubReleaseScheduleSource(this.gitHub,
			List.of("spring-projects", "spring-cloud"));

	@Test
	void whenThereAreNoRepositoriesThenReleaseSchedulesIsEmpty() {
		List<ReleaseSchedule> releases = this.source.get();
		assertThat(releases).isEmpty();
		verify(this.gitHub).getRepositories("spring-projects", null);
		verify(this.gitHub).getRepositories("spring-cloud", null);
	}

	@Test
	void whenThereAreNoMilestonesThenReleaseSchedulesHaveNoReleases() {
		Repository springBoot = repository("spring-projects", "spring-boot");
		given(this.gitHub.getRepositories("spring-projects", null)).willReturn(page(springBoot));
		Repository springCloudCommons = repository("spring-cloud", "spring-cloud-commons");
		given(this.gitHub.getRepositories("spring-cloud", null)).willReturn(page(springCloudCommons));
		List<ReleaseSchedule> releaseSchedules = this.source.get();
		assertThat(releaseSchedules).hasSize(2);
		assertThat(releaseSchedules).first().satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Boot");
			assertThat(schedule.getReleases()).isEmpty();
		});
		assertThat(releaseSchedules).element(1).satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Cloud Commons");
			assertThat(schedule.getReleases()).isEmpty();
		});
	}

	@Test
	void whenMilestonesAreScheduledThenThereIsOneReleasePerMilestone() {
		Repository springBoot = repository("spring-projects", "spring-boot");
		given(this.gitHub.getRepositories("spring-projects", null)).willReturn(page(springBoot));
		Repository springCloudCommons = repository("spring-cloud", "spring-cloud-commons");
		given(this.gitHub.getRepositories("spring-cloud", null)).willReturn(page(springCloudCommons));
		given(this.gitHub.getMilestones(springBoot, null))
			.willReturn(page(new Milestone("3.3.1", ZonedDateTime.now(), State.OPEN, 1)));
		given(this.gitHub.getMilestones(springCloudCommons, null))
			.willReturn(page(new Milestone("1.2.3", ZonedDateTime.now(), State.OPEN, 1)));
		List<ReleaseSchedule> releaseSchedules = this.source.get();
		assertThat(releaseSchedules).hasSize(2);
		assertThat(releaseSchedules).first().satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Boot");
			assertThat(schedule.getReleases()).hasSize(1);
		});
		assertThat(releaseSchedules).element(1).satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Cloud Commons");
			assertThat(schedule.getReleases()).hasSize(1);
		});
	}

	@Test
	void whenARepositoryIsInternalItIsIgnoredIfItsNameDoesNotEndWithDashCommercial() {
		Repository hidden = repository("spring-projects", "hidden-repository", Visibility.INTERNAL);
		Repository springBootCommercial = repository("spring-projects", "spring-boot-commercial", Visibility.INTERNAL);
		given(this.gitHub.getRepositories("spring-projects", null)).willReturn(page(hidden, springBootCommercial));
		given(this.gitHub.getMilestones(springBootCommercial, null))
			.willReturn(page(new Milestone("2.7.21", ZonedDateTime.now(), State.OPEN, 1)));
		List<ReleaseSchedule> releaseSchedules = this.source.get();
		assertThat(releaseSchedules).singleElement().satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Boot Commercial");
			assertThat(schedule.getReleases()).hasSize(1);
		});
	}

	@Test
	void whenARepositoryIsPrivateItIsIgnoredIfItsNameDoesNotEndWithDashCommercial() {
		Repository hidden = repository("spring-projects", "hidden-repository", Visibility.PRIVATE);
		Repository springBootCommercial = repository("spring-projects", "spring-boot-commercial", Visibility.PRIVATE);
		given(this.gitHub.getRepositories("spring-projects", null)).willReturn(page(hidden, springBootCommercial));
		given(this.gitHub.getMilestones(springBootCommercial, null))
			.willReturn(page(new Milestone("2.7.21", ZonedDateTime.now(), State.OPEN, 1)));
		List<ReleaseSchedule> releaseSchedules = this.source.get();
		assertThat(releaseSchedules).singleElement().satisfies((schedule) -> {
			assertThat(schedule.getProject()).isEqualTo("Spring Boot Commercial");
			assertThat(schedule.getReleases()).hasSize(1);
		});
	}

	private Repository repository(String organization, String name) {
		return this.repository(organization, name, Visibility.PUBLIC);
	}

	private Repository repository(String organization, String name, Visibility visibility) {
		return new Repository(name, organization + "/" + name,
				"https://api.github.com/repos/%s/%s/milestones{/number}".formatted(organization, name),
				"https://github.com/%s/%s".formatted(organization, name), visibility);
	}

	@SafeVarargs
	private <T> Page<T> page(T... content) {
		return new StandardPage<>(List.of(content), null, null, () -> null);
	}

}
