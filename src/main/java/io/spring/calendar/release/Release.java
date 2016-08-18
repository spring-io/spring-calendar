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

package io.spring.calendar.release;

/**
 * A release of a project.
 *
 * @author Andy Wilkinson
 */
public class Release {

	/**
	 * Status of the release, if known.
	 *
	 */
	public static enum Status {
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
	};

	private final Project project;

	private final String name;

	private final String date;

	private final Status status;

	/**
	 * Creates a new {@code Release}.
	 *
	 * @param project the project
	 * @param name the name of the release
	 * @param date the date of the release (yyyy-mm-dd)
	 * @param status the status of the release
	 */
	public Release(Project project, String name, String date, Status status) {
		this.project = project;
		this.name = name;
		this.date = date;
		this.status = status;
	}

	Project getProject() {
		return this.project;
	}

	String getName() {
		return this.name;
	}

	String getDate() {
		return this.date;
	}

	Status getStatus() {
		return this.status;
	}
}
