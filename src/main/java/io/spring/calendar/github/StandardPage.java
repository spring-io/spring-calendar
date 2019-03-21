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
import java.util.function.Supplier;

/**
 * Standard implementation of {@link Page}.
 *
 * @param <T> the type of the contents of the page
 * @author Andy Wilkinson
 */
class StandardPage<T> implements Page<T> {

	private List<T> content;

	private String url;

	private String etag;

	private Supplier<Page<T>> nextSupplier;

	/**
	 * Creates a new {@code StandardPage} that has the given {@code content}. The page was
	 * retrieved from the given {@code url} and has the given {@code etag}. The given
	 * {@code nextSupplier} will be used to obtain the next page {@link #next when
	 * requested}.
	 *
	 * @param content the content
	 * @param url the url
	 * @param etag the etag
	 * @param nextSupplier the supplier of the next page
	 */
	StandardPage(List<T> content, String url, String etag,
			Supplier<Page<T>> nextSupplier) {
		this.content = content;
		this.url = url;
		this.etag = etag;
		this.nextSupplier = nextSupplier;
	}

	@Override
	public Page<T> next() {
		return this.nextSupplier.get();
	}

	@Override
	public List<T> getContent() {
		return this.content;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public String getEtag() {
		return this.etag;
	}

}
