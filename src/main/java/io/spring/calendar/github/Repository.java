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

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A minimal represenation of a GitHub repository.
 *
 * @author Andy Wilkinson
 */
class Repository {

	private final String name;

	private final String fullName;

	private final String displayName;

	private final URL milestonesUrl;

	private final URL htmlUrl;

	@JsonCreator
	Repository(@JsonProperty("name") String name,
			@JsonProperty("full_name") String fullName,
			@JsonProperty("milestones_url") String milestonesUrl,
			@JsonProperty("html_url") String htmlUrl) {
		this.name = name;
		this.fullName = name;
		this.displayName = capitalize(name.replace('-', ' '));
		this.milestonesUrl = sanitizeUrl(milestonesUrl);
		this.htmlUrl = sanitizeUrl(htmlUrl);
	}

	String getName() {
		return this.name;
	}

	String getFullName() {
		return this.fullName;
	}

	String getDisplayName() {
		return this.displayName;
	}

	URL getMilestonesUrl() {
		return this.milestonesUrl;
	}

	URL getHtmlUrl() {
		return this.htmlUrl;
	}

	private static String capitalize(String input) {
		StringWriter output = new StringWriter();
		for (int i = 0; i < input.length(); i++) {
			if (i == 0 || i > 0 && input.charAt(i - 1) == ' ') {
				output.append(Character.toUpperCase(input.charAt(i)));
			}
			else {
				output.append(input.charAt(i));
			}
		}
		return output.toString();
	}

	private static URL sanitizeUrl(String url) {
		int index = url.indexOf('{');
		try {
			if (index < 0) {
				return URI.create(url).toURL();
			}
			return URI.create(url.substring(0, index)).toURL();
		}
		catch (MalformedURLException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
