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

import java.util.List;

/**
 * The {@link Release Releases} for a project.
 *
 * @author Andy Wilkinson
 */
public final class ProjectReleases {

	private final String project;

	private final List<Release> releases;

	/**
	 * Creates a new {@code ProjectReleases} for the given {@code project} with the given
	 * {@code releases}.
	 *
	 * @param project The project
	 * @param releases The releases
	 */
	public ProjectReleases(String project, List<Release> releases) {
		this.project = project;
		this.releases = releases;
	}

	/**
	 * Returns the project.
	 * @return the project
	 */
	public String getProject() {
		return this.project;
	}

	/**
	 * Returns the releases.
	 * @return the releases
	 */
	public List<Release> getReleases() {
		return this.releases;
	}

}
