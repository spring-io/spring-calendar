/*
 * Copyright 2016 the original author or authors.
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

/**
 * Standard implementation of {@code ProjectNameAliaser}.
 *
 * @author Andy Wilkinson
 */
class StandardProjectNameAliaser implements ProjectNameAliaser {

	private final Map<String, String> aliases;

	StandardProjectNameAliaser(Map<String, String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public String apply(String name) {
		return this.aliases.getOrDefault(name, name);
	}

}
