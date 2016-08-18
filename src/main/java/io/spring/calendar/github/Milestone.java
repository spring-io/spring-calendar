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

package io.spring.calendar.github;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A minimal representation of a GitHub issues milestone.
 *
 * @author Andy Wilkinson
 */
class Milestone {

	/**
	 * State of the milestone according to github.
	 *
	 */
	static enum State {
		open, closed
	};

	private final String title;

	private final ZonedDateTime dueOn;

	private final State state;

	@JsonCreator
	Milestone(@JsonProperty("title") String title,
			@JsonProperty("due_on") ZonedDateTime dueOn,
			@JsonProperty("state") State state) {
		this.title = title;
		this.dueOn = dueOn == null ? null : dueOn.withZoneSameInstant(ZoneId.of("UTC"));
		this.state = state;
	}

	String getTitle() {
		return this.title;
	}

	ZonedDateTime getDueOn() {
		return this.dueOn;
	}

	public State getState() {
		return this.state;
	}

}
