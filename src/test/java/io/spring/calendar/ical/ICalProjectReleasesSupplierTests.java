/*
 * Copyright 2016-2020 the original author or authors.
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

import io.spring.calendar.release.ProjectReleases;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link ICalProjectReleasesSupplier}.
 *
 * @author Andy Wilkinson
 */
@RestClientTest(components = ICalProjectReleasesSupplier.class)
@RunWith(SpringRunner.class)
public class ICalProjectReleasesSupplierTests {

	private static final String SPRING_DATA_CALENDAR = "https://outlook.office365.com/owa/calendar/9d3cecb6098e4d7d884561cf288d70b7@vmware.com/4f8a123268f047d0b0b9319040506e2a3791298319254920500/calendar.ics";

	@Autowired
	private ICalProjectReleasesSupplier supplier;

	@Autowired
	private MockRestServiceServer server;

	@Test
	public void iCalCanBeRetrieved() {
		this.server.expect(requestTo(SPRING_DATA_CALENDAR)).andRespond(withSuccess()
				.body(new FileSystemResource("src/test/resources/io/spring/calendar/ical/spring-data.ics")));
		List<ProjectReleases> allProjectReleases = this.supplier.get();
		assertThat(allProjectReleases).hasSize(1);
		ProjectReleases projectReleases = allProjectReleases.get(0);
		assertThat(projectReleases.getProject()).isEqualTo("Spring Data");
		assertThat(projectReleases.getReleases()).hasSize(6);
	}

	@Test
	public void whenAFailureOccursRetrievingAnICalThenItIsHandledGracefully() {
		this.server.expect(requestTo(SPRING_DATA_CALENDAR)).andRespond(withServerError());
		List<ProjectReleases> allProjectReleases = this.supplier.get();
		assertThat(allProjectReleases).hasSize(1);
		ProjectReleases projectReleases = allProjectReleases.get(0);
		assertThat(projectReleases.getProject()).isEqualTo("Spring Data");
		assertThat(projectReleases.getReleases()).hasSize(0);
	}

}
