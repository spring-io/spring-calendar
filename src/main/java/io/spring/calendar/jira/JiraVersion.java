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

package io.spring.calendar.jira;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A minimal representation of a Jira version.
 *
 * @author Andy Wilkinson
 */
class JiraVersion {

	private final String id;

	private final String name;

	private final String releaseDate;

	private final boolean released;

	@JsonCreator
	JiraVersion(@JsonProperty("id") String id, @JsonProperty("name") String name,
			@JsonProperty("releaseDate") String releaseDate, @JsonProperty("released") boolean released) {
		this.id = id;
		this.name = name;
		this.releaseDate = releaseDate;
		this.released = released;
	}

	String getId() {
		return this.id;
	}

	String getName() {
		return this.name;
	}

	String getReleaseDate() {
		return this.releaseDate;
	}

	boolean isReleased() {
		return this.released;
	}

}
