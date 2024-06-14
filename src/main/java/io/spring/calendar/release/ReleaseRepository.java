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

import java.util.Date;
import java.util.List;

import io.spring.calendar.release.Release.Type;

/**
 * A repository for {@link Release Releases}.
 *
 * @author Andy Wilkinson
 */
public interface ReleaseRepository {

	/**
	 * Sets the repository's releases to the given {@code releases}.
	 * @param releases the releases
	 */
	void set(List<Release> releases);

	/**
	 * Returns all of the releases known to the repository of the given {@code type}.
	 * @param type the release type. May be {@code null} to return all known releases.
	 * @return the releases
	 */
	List<Release> findAllOfType(Type type);

	/**
	 * Returns all of the releases known to the repository with a release date in the
	 * given period.
	 * @param type the release type. May be {@code null} to return all known releases in
	 * the given period.
	 * @param start the start of the period
	 * @param end the end of the period
	 * @return the releases in the period
	 */
	List<Release> findAllOfTypeInPeriod(Type type, Date start, Date end);

}
