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

package io.spring.calendar.test;

import java.io.IOException;
import java.lang.reflect.Method;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;

/**
 * A {@link ResponseCreator} that returns a successful response with the body loaded from
 * a file named <code>{testClass.getSimpleName()}-{testMethod.getName()}.json}</code> in
 * the test class's package.
 *
 * @author Andy Wilkinson
 */
public class TestMethodResponseCreator implements ResponseCreator {

	private Class<?> testClass;

	private Method testMethod;

	@Override
	public ClientHttpResponse createResponse(ClientHttpRequest request) throws IOException {
		return MockRestResponseCreators.withSuccess().body(getCurrentResource()).contentType(MediaType.APPLICATION_JSON)
				.createResponse(request);
	}

	void setTest(Class<?> testClass, Method testMethod) {
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	private Resource getCurrentResource() {
		String resourceName = this.testClass.getSimpleName() + "-" + this.testMethod.getName() + ".json";
		return new UrlResource(this.testClass.getResource(resourceName));
	}

}
