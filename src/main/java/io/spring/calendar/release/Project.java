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

import java.net.URL;

/**
 * A project.
 *
 * @author Andy Wilkinson
 */
public class Project {

	private final String name;

	private final URL url;

	/**
	 * Creates a new project.
	 *
	 * @param name of the project
	 */
	public Project(String name) {
		this(name, null);
	}

	/**
	 * Creates a new project.
	 *
	 * @param name the name of the project
	 * @param url the URL of the project
	 */
	public Project(String name, URL url) {
		this.name = name;
		this.url = url;
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
	 * Returns the URL of the project.
	 *
	 * @return the project's URL
	 */
	public URL getUrl() {
		return this.url;
	}

}
