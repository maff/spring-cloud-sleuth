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

package org.springframework.cloud.sleuth.autoconfig.instrument.web.client.feign;

import feign.Client;
import feign.Feign;
import feign.okhttp.OkHttpClient;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.client.feign.FeignContextBeanPostProcessor;
import org.springframework.cloud.sleuth.instrument.web.client.feign.OkHttpFeignClientBeanPostProcessor;
import org.springframework.cloud.sleuth.instrument.web.client.feign.SleuthFeignBuilder;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceFeignAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} enables span information propagation when using Feign.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.sleuth.feign.enabled", matchIfMissing = true)
@ConditionalOnClass({ Client.class, FeignContext.class })
@ConditionalOnBean(Tracer.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class TraceFeignClientAutoConfiguration {

	@Bean
	TraceFeignAspect traceFeignAspect(BeanFactory beanFactory) {
		return new TraceFeignAspect(beanFactory);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(name = "spring.sleuth.feign.processor.enabled", matchIfMissing = true)
	protected static class FeignBeanPostProcessorConfiguration {

		@Bean
		static FeignContextBeanPostProcessor feignContextBeanPostProcessor(BeanFactory beanFactory) {
			return new FeignContextBeanPostProcessor(beanFactory);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(OkHttpClient.class)
	protected static class OkHttpClientFeignBeanPostProcessorConfiguration {

		@Bean
		static OkHttpFeignClientBeanPostProcessor okHttpFeignClientBeanPostProcessor(BeanFactory beanFactory) {
			return new OkHttpFeignClientBeanPostProcessor(beanFactory);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Conditional(CircuitBreakerMissingOrDisabledCondition.class)
	protected static class CircuitBreakerMissingConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@Scope("prototype")
		Feign.Builder feignBuilder(BeanFactory beanFactory) {
			return SleuthFeignBuilder.builder(beanFactory);
		}

	}

	private static class CircuitBreakerMissingOrDisabledCondition extends AnyNestedCondition {

		CircuitBreakerMissingOrDisabledCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnMissingClass("org.springframework.cloud.client.circuitbreaker.CircuitBreaker")
		static class NoCircuitBreakerClassFound {

		}

		@ConditionalOnProperty(value = "feign.circuitbreaker.enabled", havingValue = "false", matchIfMissing = true)
		static class CircuitBreakerDisabled {

		}

	}

}
