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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A minimal representation of a project in a JIRA instance.
 *
 * @author Andy Wilkinson
 */
public class JiraProject {

	private String name;

	private URI uri;

	/**
	 * Creates a new project with the given {@code name} and {@code url}.
	 *
	 * @param name the project's name
	 * @param uri the project's URI
	 */
	@JsonCreator
	public JiraProject(@JsonProperty("name") String name, @JsonProperty("self") URI uri) {
		this.name = name;
		this.uri = uri;
	}

	/**
	 * Returns the name of the project.
	 *
	 * @return the project's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the URI of the project.
	 * @return the project's URI
	 */
	public URI getUri() {
		return this.uri;
	}

}
