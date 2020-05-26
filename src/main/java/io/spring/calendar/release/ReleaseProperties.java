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

package io.spring.calendar.release;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Release-related configuration properties.
 *
 * @author Andy Wilkinson
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "calendar.release")
class ReleaseProperties {

	private final Map<String, String> projectAliases;

	ReleaseProperties(Map<String, String> projectAliases) {
		this.projectAliases = projectAliases;
	}

	Map<String, String> getProjectAliases() {
		return this.projectAliases;
	}

}
