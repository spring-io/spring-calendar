/*
 * Copyright 2016-2023 the original author or authors.
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

package io.spring.calendar;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class for the Spring Calendar.
 *
 * @author Andy Wilkinson
 */
@SpringBootApplication
@EnableScheduling
public class SpringCalendar {

	public static void main(String[] args) {
		SpringApplication.run(SpringCalendar.class, args);
	}

	@Bean
	public RestTemplateCustomizer httpClientTimeoutRestTemplateCustomizer() {
		return (restTemplate) -> {
			RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(30000)
				.setConnectionRequestTimeout(30000)
				.setSocketTimeout(30000)
				.build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
					httpClient);
			restTemplate.setRequestFactory(requestFactory);
		};
	}

}
