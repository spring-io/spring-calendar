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

package io.spring.calendar.github;

import java.util.List;

/**
 * A page of results.
 *
 * @param <T> the type of the contents of the page
 * @author Andy Wilkinson
 */
interface Page<T> {

	/**
	 * Returns the next page, if any.
	 *
	 * @return The next page or {@code null}
	 */
	Page<T> next();

	/**
	 * Returns the contents of the page.
	 *
	 * @return the contents
	 */
	List<T> getContent();

	/**
	 * Returns the URL of the page.
	 * @return the page's URL
	 */
	String getUrl();

	/**
	 * Returns the etag of the page.
	 * @return the page's etag
	 */
	String getEtag();

}
