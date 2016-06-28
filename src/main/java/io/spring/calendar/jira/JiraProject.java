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

package io.spring.calendar.jira;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * A project managed in JIRA.
 *
 * @author Andy Wilkinson
 */
@Entity
class JiraProject {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private String key;

	JiraProject() {

	}

	long getId() {
		return this.id;
	}

	String getName() {
		return this.name;
	}

	String getKey() {
		return this.key;
	}

}
