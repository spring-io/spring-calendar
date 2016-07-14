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

/**
 * Operations that can be performed against the GitHub API.
 *
 * @author Andy Wilkinson
 */
interface GitHubOperations {

	/**
	 * Returns the milestones in the {@code repository} owned by the given
	 * {@code organization}.
	 *
	 * @param organization the name of the organization
	 * @param repository the name of the repository
	 * @param eTag the ETag of an earlier response, or {@code null}
	 * @return the page of milestones or {@code null} if unmodified based on the
	 * {@code eTag}
	 */
	Page<Milestone> getMilestones(String organization, String repository, String eTag);

}
