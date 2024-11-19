/*
 * Copyright 2024 the original author or authors.
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

import java.util.Locale;

import io.spring.calendar.release.Release.Type;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@link WebMvcConfigurer} for working with {@link Release releases}.
 *
 * @author Andy Wilkinson
 */
@Component
class ReleaseWebMvcConfigurer implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		ApplicationConversionService.addApplicationConverters(registry);
		registry.addConverter(String.class, Type.class, (input) -> {
			if ("commercial".equalsIgnoreCase(input)) {
				return Type.ENTERPRISE;
			}
			return Type.valueOf(input.toUpperCase(Locale.ROOT));
		});
	}

}
