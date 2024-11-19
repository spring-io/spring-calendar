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

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * A release of a project.
 *
 * @author Andy Wilkinson
 */
public class Release {

	private final String project;

	private final String name;

	private final String date;

	private final Status status;

	private final URL url;

	private final Type type;

	/**
	 * Creates a new {@code Release}.
	 * @param project the project
	 * @param name the name of the release
	 * @param date the date of the release (yyyy-mm-dd)
	 * @param status the status of the release
	 * @param url the URL of the release
	 * @param type the type of the release
	 */
	public Release(String project, String name, String date, Status status, URL url, Type type) {
		this.project = project;
		this.name = name;
		this.date = date;
		this.status = status;
		this.url = url;
		this.type = type;
	}

	String getProject() {
		return this.project;
	}

	String getDate() {
		return this.date;
	}

	Status getStatus() {
		return this.status;
	}

	boolean isOverdue() {
		return this.status == Status.OPEN
				&& LocalDate.now(ZoneId.of("Europe/London")).isAfter(LocalDate.parse(this.date));
	}

	URL getUrl() {
		return this.url;
	}

	Type getType() {
		return this.type;
	}

	String getDescription() {
		String description = this.project + " " + this.name;
		if (this.type == Type.ENTERPRISE) {
			description = description + " (Enterprise)";
		}
		return description;
	}

	Release withProject(String project) {
		return new Release(project, this.name, this.date, this.status, this.url, this.type);
	}

	/**
	 * Type of the release.
	 */
	public enum Type {

		/**
		 * An OSS release.
		 */
		OSS,

		/**
		 * An enterprise release.
		 */
		ENTERPRISE;

	}

	/**
	 * Status of the release, if known.
	 *
	 */
	public enum Status {

		/**
		 * The release is open (not yet closed).
		 */
		OPEN,
		/**
		 * The release is closed (completed).
		 */
		CLOSED,
		/**
		 * The release status is not known.
		 */
		UNKNOWN

	}

}
