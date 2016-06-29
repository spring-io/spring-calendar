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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * A release of a project.
 *
 * @author Andy Wilkinson
 */
@Entity
public class Release {

	@Id
	@GeneratedValue
	private long id;

	private String project;

	private String name;

	private String date;

	Release() {

	}

	/**
	 * Creates a new {@code Release}.
	 *
	 * @param project the project
	 * @param name the name of the release
	 * @param date the date of the release (yyyy-mm-dd)
	 */
	public Release(String project, String name, String date) {
		this.project = project;
		this.name = name;
		this.date = date;
	}

	long getId() {
		return this.id;
	}

	String getProject() {
		return this.project;
	}

	String getName() {
		return this.name;
	}

	String getDate() {
		return this.date;
	}

}
