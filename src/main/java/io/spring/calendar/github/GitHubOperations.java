/*
 * Copyright 2016-2024 the original author or authors.
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

/**
 * Operations that can be performed against the GitHub API.
 *
 * @author Andy Wilkinson
 */
interface GitHubOperations {

	/**
	 * Returns the milestones in the given {@code repository}.
	 * @param repository the repository
	 * @param earlierResponse the first page of an earlier response that can be used to
	 * perform conditional requests, or {@code null}.
	 * @return the page of milestones
	 */
	Page<Milestone> getMilestones(Repository repository, Page<Milestone> earlierResponse);

	/**
	 * Returns the repositories of the given {@code organization}.
	 * @param organization the name of the organization
	 * @param earlierResponse the first page of an earlier response that can be used to
	 * perform conditional requests, or {@code null}.
	 * @return the page of repositories
	 */
	Page<Repository> getRepositories(String organization, Page<Repository> earlierResponse);

}
