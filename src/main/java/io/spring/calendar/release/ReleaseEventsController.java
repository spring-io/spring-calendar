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

package io.spring.calendar.release;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.spring.calendar.release.Release.Status;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for exposing {@link Release Releases} as Full Calendar events.
 *
 * @author Andy Wilkinson
 * @author Brian Clozel
 */
@RestController
@RequestMapping("/releases")
class ReleaseEventsController {

	private final ReleaseRepository releaseRepository;

	ReleaseEventsController(ReleaseRepository releaseRepository) {
		this.releaseRepository = releaseRepository;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	List<Map<String, Object>> releases(@RequestParam String start, @RequestParam String end) throws ParseException {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(start);
		Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(end);
		return this.releaseRepository.findAllInPeriod(startDate, endDate).stream().map((release) -> {
			Map<String, Object> event = new HashMap<>();
			event.put("title", release.getProject() + " " + release.getName());
			event.put("allDay", true);
			event.put("start", release.getDate());
			if (release.getUrl() != null) {
				event.put("url", release.getUrl());
			}
			if (release.getStatus() == Status.CLOSED) {
				event.put("backgroundColor", "#6db33f");
			}
			else if (release.isOverdue()) {
				event.put("backgroundColor", "#d14");
			}
			return event;
		}).toList();
	}

	@ExceptionHandler
	ResponseEntity<String> handleDateParseException(ParseException exc) {
		return ResponseEntity.badRequest().body(exc.getMessage());
	}

}
