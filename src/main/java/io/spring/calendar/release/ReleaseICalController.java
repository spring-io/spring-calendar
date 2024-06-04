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

package io.spring.calendar.release;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for exposing {@link Release Releases} as an iCalendar-format download.
 *
 * @author Andy Wilkinson
 */
@RestController
@RequestMapping("/ical")
class ReleaseICalController {

	private final ReleaseRepository releaseRepository;

	ReleaseICalController(ReleaseRepository releaseRepository) {
		this.releaseRepository = releaseRepository;
	}

	@RequestMapping(produces = "text/calendar")
	String calendar() {
		ICalendar calendar = new ICalendar();
		calendar.setExperimentalProperty("X-WR-CALNAME", "Spring Releases");
		this.releaseRepository.findAll().stream().map(this::createEvent).forEach(calendar::addEvent);
		return Biweekly.write(calendar).go();
	}

	private VEvent createEvent(Release release) {
		VEvent event = new VEvent();
		event.setSummary(release.getProject() + " " + release.getName());
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(release.getDate());
			event.setDateStart(date, false);
			event.setDateEnd(date, false);
		}
		catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		return event;
	}

}
