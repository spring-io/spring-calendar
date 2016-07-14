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

package io.spring.calendar.ical;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A project available as an iCalendar.
 *
 * @author Andy Wilkinson
 */
@Entity
@Table(name = "ical_project")
class ICalProject {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private URL calendarUrl;

	ICalProject() {

	}

	long getId() {
		return this.id;
	}

	String getName() {
		return this.name;
	}

	URL getCalendarUrl() {
		return this.calendarUrl;
	}

}
