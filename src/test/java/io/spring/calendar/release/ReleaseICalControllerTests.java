/*
 * Copyright 2024-2025 the original author or authors.
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

package io.spring.calendar.release;

import java.util.Arrays;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.Release.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link ReleaseICalController}.
 *
 * @author Andy Wilkinson
 */
@WebMvcTest
class ReleaseICalControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ReleaseRepository releases;

	@Test
	void givenNoReleasesWhenIcalIsCalledThenSingleEmptyCalendarIsReturned() throws Exception {
		String responseBody = this.mvc.perform(MockMvcRequestBuilders.get("/ical"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		List<ICalendar> calendars = Biweekly.parse(responseBody).all();
		assertThat(calendars).singleElement().satisfies((calendar) -> {
			assertThat(calendar.getExperimentalProperty("X-WR-CALNAME").getValue()).isEqualTo("Spring Releases");
			assertThat(calendar.getEvents()).isEmpty();
		});
	}

	@Test
	void givenSomeReleasesWhenIcalIsCalledThenSingleCalendarIsReturnedWithOneEventPerRelease() throws Exception {
		given(this.releases.findAllOfType(null)).willReturn(
				Arrays.asList(new Release("Spring Boot", "2.7.21", "2024-06-01", Status.CLOSED, null, Type.ENTERPRISE),
						new Release("Spring Boot", "3.3.1", "2024-06-01", Status.CLOSED, null, Type.OSS)));
		List<ICalendar> calendars = calendars("/ical");
		assertThat(calendars).singleElement().satisfies((calendar) -> {
			assertThat(calendar.getExperimentalProperty("X-WR-CALNAME").getValue()).isEqualTo("Spring Releases");
			assertThat(calendar.getEvents()).hasSize(2);
		});
	}

	@Test
	void givenSomeReleasesWhenIcalIsCalledWithCommercialThenSingleCalendarIsReturnedWithOneEventPerEnterpriseRelease()
			throws Exception {
		given(this.releases.findAllOfType(Type.ENTERPRISE)).willReturn(Arrays
			.asList(new Release("Spring Boot", "2.7.21", "2024-06-01", Status.CLOSED, null, Type.ENTERPRISE)));
		List<ICalendar> calendars = calendars("/ical?type=commercial");
		assertThat(calendars).singleElement().satisfies((calendar) -> {
			assertThat(calendar.getExperimentalProperty("X-WR-CALNAME").getValue())
				.isEqualTo("Spring Enterprise Releases");
			assertThat(calendar.getEvents()).hasSize(1);
		});
	}

	@Test
	void givenSomeReleasesWhenIcalIsCalledWithEnterpriseThenSingleCalendarIsReturnedWithOneEventPerEnterpriseRelease()
			throws Exception {
		given(this.releases.findAllOfType(Type.ENTERPRISE)).willReturn(Arrays
			.asList(new Release("Spring Boot", "2.7.21", "2024-06-01", Status.CLOSED, null, Type.ENTERPRISE)));
		List<ICalendar> calendars = calendars("/ical?type=enterprise");
		assertThat(calendars).singleElement().satisfies((calendar) -> {
			assertThat(calendar.getExperimentalProperty("X-WR-CALNAME").getValue())
				.isEqualTo("Spring Enterprise Releases");
			assertThat(calendar.getEvents()).hasSize(1);
		});
	}

	@Test
	void givenSomeReleasesWhenIcalIsCalledWithOssThenSingleCalendarIsReturnedWithOneEventPerOssRelease()
			throws Exception {
		given(this.releases.findAllOfType(Type.OSS)).willReturn(
				Arrays.asList(new Release("Spring Boot", "3.3.1", "2024-06-01", Status.CLOSED, null, Type.OSS)));
		List<ICalendar> calendars = calendars("/ical?type=oss");
		assertThat(calendars).singleElement().satisfies((calendar) -> {
			assertThat(calendar.getExperimentalProperty("X-WR-CALNAME").getValue()).isEqualTo("Spring OSS Releases");
			assertThat(calendar.getEvents()).hasSize(1);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://spring.io", "https://enterprise.spring.io" })
	void icalAllowsCrossOriginRequestsFromSpringIo(String origin) throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/ical").header("Origin", origin))
			.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
	}

	@Test
	void icalDoesNotAllowCrossOriginRequestsFromElsewhere() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/ical").header("Origin", "https://example.com"))
			.andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}

	private List<ICalendar> calendars(String url) throws Exception {
		String responseBody = this.mvc.perform(MockMvcRequestBuilders.get(url))
			.andReturn()
			.getResponse()
			.getContentAsString();
		return Biweekly.parse(responseBody).all();
	}

}
