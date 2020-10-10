/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.sleuth.otel.instrument.discoveryexception;

import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.otel.ArrayListSpanProcessor;
import org.springframework.cloud.sleuth.otel.OtelTestSpanHandler;
import org.springframework.cloud.sleuth.test.TestSpanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = { WebClientDiscoveryExceptionTests.Config.class,
		org.springframework.cloud.sleuth.instrument.web.client.discoveryexception.WebClientDiscoveryExceptionTests.TestConfiguration.class },
		webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "spring.application.name=exceptionservice" })
@DirtiesContext
public class WebClientDiscoveryExceptionTests extends
		org.springframework.cloud.sleuth.instrument.web.client.discoveryexception.WebClientDiscoveryExceptionTests {

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		TestSpanHandler testSpanHandlerSupplier() {
			return new OtelTestSpanHandler(new ArrayListSpanProcessor());
		}

		@Bean
		Sampler alwaysSampler() {
			return Samplers.alwaysOn();
		}

	}

}
