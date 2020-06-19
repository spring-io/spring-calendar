/*
 * Copyright 2016-2019 the original author or authors.
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

/**
 * A project available as an iCalendar.
 *
 * @author Andy Wilkinson
 */
class ICalProject {

	private final String name;

	private final URI calendarUri;

	ICalProject(String name, URI calendarUri) {
		this.name = name;
		this.calendarUri = calendarUri;
	}

	String getName() {
		return this.name;
	}

	URI getCalendarUri() {
		return this.calendarUri;
	}

}
