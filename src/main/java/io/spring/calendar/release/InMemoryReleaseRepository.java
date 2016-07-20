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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Repository;

/**
 * An in-memory implementation of {@code ReleaseRepository}.
 *
 * @author Andy Wilkinson
 */
@Repository
class InMemoryReleaseRepository implements ReleaseRepository {

	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

	private List<Release> releases = Collections.emptyList();

	@Override
	public void set(List<Release> releases) {
		this.lock.writeLock().lock();
		try {
			this.releases = new ArrayList<>(releases);
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public List<Release> findAll() {
		this.lock.readLock().lock();
		try {
			return Collections.unmodifiableList(this.releases);
		}
		finally {
			this.lock.readLock().unlock();
		}
	}

}
