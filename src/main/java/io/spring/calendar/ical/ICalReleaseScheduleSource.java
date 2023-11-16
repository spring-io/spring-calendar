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

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import io.spring.calendar.release.Release;
import io.spring.calendar.release.Release.Status;
import io.spring.calendar.release.ReleaseSchedule;
import io.spring.calendar.release.ReleaseScheduleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

/**
 * A {@link ReleaseScheduleSource} for {@link ICalProject ICalProjects}.
 *
 * @author Andy Wilkinson
 */
@Component
class ICalReleaseScheduleSource implements ReleaseScheduleSource {

	private final Logger log = LoggerFactory.getLogger(ICalReleaseScheduleSource.class);

	private final List<ICalProject> projects = createProjects();

	private final RestOperations rest;

	private final Map<ICalProject, List<ICalendar>> previousCalendars = new HashMap<>();

	ICalReleaseScheduleSource(RestTemplateBuilder restTemplateBuilder) {
		this.rest = restTemplateBuilder.build();
	}

	@Override
	public List<ReleaseSchedule> get() {
		return this.projects.stream().map(this::createReleaseSchedule).toList();
	}

	private ReleaseSchedule createReleaseSchedule(ICalProject project) {
		List<Release> releases = parseICalendars(project).stream()
			.flatMap(this::calendarEvents)
			.map((event) -> createRelease(project, event))
			.toList();
		ReleaseSchedule releaseSchedule = new ReleaseSchedule(project.getName(), releases);
		return releaseSchedule;
	}

	private Stream<VEvent> calendarEvents(ICalendar calendar) {
		return calendar.getEvents().stream();
	}

	private List<ICalendar> parseICalendars(ICalProject project) {
		try {
			List<ICalendar> calendars = Biweekly.parse(this.rest.getForObject(project.getCalendarUri(), String.class))
				.all();
			this.previousCalendars.put(project, calendars);
			return calendars;
		}
		catch (RestClientException ex) {
			this.log.warn("Failed to retrieve calendar from '{}'", project.getCalendarUri(), ex);
			return this.previousCalendars.getOrDefault(project, Collections.emptyList());
		}
	}

	private Release createRelease(ICalProject project, VEvent event) {
		String name = event.getSummary().getValue();
		if (name.startsWith(project.getName())) {
			name = name.substring(project.getName().length()).trim();
		}
		return new Release(project.getName(), name,
				new SimpleDateFormat("yyyy-MM-dd").format(event.getDateStart().getValue()), Status.UNKNOWN, null);
	}

	private static List<ICalProject> createProjects() {
		return Arrays.asList(new ICalProject("Spring Data", URI.create(
				"https://outlook.office365.com/owa/calendar/9d3cecb6098e4d7d884561cf288d70b7@vmware.com/4f8a123268f047d0b0b9319040506e2a3791298319254920500/calendar.ics")));
	}

}
