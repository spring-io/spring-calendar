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

package io.spring.calendar.test;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * A {@link TestExecutionListener} that enables the usage of
 * {@link TestMethodResponseCreator}.
 *
 * @author Andy Wilkinson
 */
public final class TestMethodResponseTestExecutionListener
		extends AbstractTestExecutionListener {

	private final TestMethodResponseCreator testMethodResponseCreator = new TestMethodResponseCreator();

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		((ConfigurableBeanFactory) testContext.getApplicationContext()
				.getAutowireCapableBeanFactory()).registerSingleton(
						"testMethodResponseCreator", this.testMethodResponseCreator);
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		this.testMethodResponseCreator.setTest(testContext.getTestClass(),
				testContext.getTestMethod());
	}

}
