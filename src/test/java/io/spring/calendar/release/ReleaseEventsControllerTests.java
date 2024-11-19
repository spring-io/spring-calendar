/*
 * Copyright 2024 the original author or authors.
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

import java.text.SimpleDateFormat;
import java.util.Arrays;

import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.Release.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link ReleaseEventsController}.
 *
 * @author Andy Wilkinson
 */
@WebMvcTest
class ReleaseEventsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ReleaseRepository releases;

	@Test
	void whenReleasesIsCalledForPeriodWithNoReleasesThenEmptyListIsReturned() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content().json("[]"));
	}

	@Test
	void whenReleasesIsCalledThenReleasesInPeriodAreReturned() throws Exception {
		String start = "2024-06-01";
		String end = "2024-06-02";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		given(this.releases.findAllOfTypeInPeriod(null, format.parse(start), format.parse(end))).willReturn(
				Arrays.asList(new Release("Spring Boot", "3.3.1", "2024-06-01", Status.CLOSED, null, Type.OSS)));
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content()
				.json("[{\"allDay\":true,\"backgroundColor\":\"#6db33f\",\"start\":\"2024-06-01\",\"title\":\"Spring Boot 3.3.1\"}]"));
	}

	@Test
	void whenReleasesWithCommercialTypeIsCalledThenEnterpriseReleasesInPeriodAreReturned() throws Exception {
		String start = "2024-06-01";
		String end = "2024-06-02";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		given(this.releases.findAllOfTypeInPeriod(Type.ENTERPRISE, format.parse(start), format.parse(end)))
			.willReturn(Arrays
				.asList(new Release("Spring Boot", "2.7.21", "2024-06-01", Status.CLOSED, null, Type.ENTERPRISE)));
		given(this.releases.findAllOfTypeInPeriod(Type.OSS, format.parse(start), format.parse(end))).willReturn(
				Arrays.asList(new Release("Spring Boot", "3.3.1", "2024-06-01", Status.CLOSED, null, Type.OSS)));
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?type=commercial&start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content()
				.json("[{\"allDay\":true,\"backgroundColor\":\"#6db33f\",\"start\":\"2024-06-01\",\"title\":\"Spring Boot 2.7.21 (Enterprise)\"}]"));
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?type=oss&start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content()
				.json("[{\"allDay\":true,\"backgroundColor\":\"#6db33f\",\"start\":\"2024-06-01\",\"title\":\"Spring Boot 3.3.1\"}]"));
	}

	@Test
	void whenReleasesWithEnterpriseTypeIsCalledThenEnterpriseReleasesInPeriodAreReturned() throws Exception {
		String start = "2024-06-01";
		String end = "2024-06-02";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		given(this.releases.findAllOfTypeInPeriod(Type.ENTERPRISE, format.parse(start), format.parse(end)))
			.willReturn(Arrays
				.asList(new Release("Spring Boot", "2.7.21", "2024-06-01", Status.CLOSED, null, Type.ENTERPRISE)));
		given(this.releases.findAllOfTypeInPeriod(Type.OSS, format.parse(start), format.parse(end))).willReturn(
				Arrays.asList(new Release("Spring Boot", "3.3.1", "2024-06-01", Status.CLOSED, null, Type.OSS)));
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?type=enterprise&start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content()
				.json("[{\"allDay\":true,\"backgroundColor\":\"#6db33f\",\"start\":\"2024-06-01\",\"title\":\"Spring Boot 2.7.21 (Enterprise)\"}]"));
		this.mvc.perform(MockMvcRequestBuilders.get("/releases?type=oss&start=2024-06-01&end=2024-06-02"))
			.andExpect(MockMvcResultMatchers.content()
				.json("[{\"allDay\":true,\"backgroundColor\":\"#6db33f\",\"start\":\"2024-06-01\",\"title\":\"Spring Boot 3.3.1\"}]"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://spring.io", "https://enterprise.spring.io" })
	void releasesAllowsCrossOriginRequestsFromSpringIo(String origin) throws Exception {
		this.mvc
			.perform(MockMvcRequestBuilders.get("/releases?start=2024-06-01&end=2024-06-02").header("Origin", origin))
			.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
	}

	@Test
	void releasesDoesNotAllowCrossOriginRequestsFromElsewhere() throws Exception {
		this.mvc
			.perform(MockMvcRequestBuilders.get("/releases?start=2024-06-01&end=2024-06-02")
				.header("Origin", "https://example.com"))
			.andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}

}
