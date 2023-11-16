/*
 * Copyright 2016-2023 the original author or authors.
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

package io.spring.calendar.ical;

import java.util.List;

import io.spring.calendar.release.ReleaseSchedule;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link ICalReleaseScheduleSource}.
 *
 * @author Andy Wilkinson
 */
class ICalReleaseScheduleSourceTests {

	private static final String SPRING_DATA_CALENDAR = "https://outlook.office365.com/owa/calendar/9d3cecb6098e4d7d884561cf288d70b7@vmware.com/4f8a123268f047d0b0b9319040506e2a3791298319254920500/calendar.ics";

	private final ICalReleaseScheduleSource supplier;

	private final MockRestServiceServer server;

	ICalReleaseScheduleSourceTests() {
		MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder().additionalCustomizers(customizer);
		this.supplier = new ICalReleaseScheduleSource(restTemplateBuilder);
		this.server = customizer.getServer();
	}

	@Test
	void iCalCanBeRetrieved() {
		this.server.expect(requestTo(SPRING_DATA_CALENDAR))
			.andRespond(withSuccess()
				.body(new FileSystemResource("src/test/resources/io/spring/calendar/ical/spring-data.ics")));
		List<ReleaseSchedule> releaseSchedules = this.supplier.get();
		assertThat(releaseSchedules).hasSize(1);
		ReleaseSchedule releaseSchedule = releaseSchedules.get(0);
		assertThat(releaseSchedule.getProject()).isEqualTo("Spring Data");
		assertThat(releaseSchedule.getReleases()).hasSize(6);
	}

	@Test
	void whenAFailureOccursRetrievingAnICalWithNoPreviousResultsThenAnEmptyListIsReturned() {
		this.server.expect(requestTo(SPRING_DATA_CALENDAR)).andRespond(withServerError());
		List<ReleaseSchedule> releaseSchedules = this.supplier.get();
		assertThat(releaseSchedules).hasSize(1);
		ReleaseSchedule releaseSchedule = releaseSchedules.get(0);
		assertThat(releaseSchedule.getProject()).isEqualTo("Spring Data");
		assertThat(releaseSchedule.getReleases()).hasSize(0);
	}

	@Test
	void whenAFailureOccursRetrievingAnICalWithPreviousResultsThenThosePreviousResultsAreReturned() {
		this.server.expect(requestTo(SPRING_DATA_CALENDAR))
			.andRespond(withSuccess()
				.body(new FileSystemResource("src/test/resources/io/spring/calendar/ical/spring-data.ics")));
		this.server.expect(requestTo(SPRING_DATA_CALENDAR)).andRespond(withServerError());
		this.supplier.get();
		List<ReleaseSchedule> releaseSchedules = this.supplier.get();
		assertThat(releaseSchedules).hasSize(1);
		ReleaseSchedule releaseSchedule = releaseSchedules.get(0);
		assertThat(releaseSchedule.getProject()).isEqualTo("Spring Data");
		assertThat(releaseSchedule.getReleases()).hasSize(6);
	}

}
