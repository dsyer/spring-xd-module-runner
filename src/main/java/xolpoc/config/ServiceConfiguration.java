/*
 * Copyright 2015 the original author or authors.
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

package xolpoc.config;

import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Bind to services, either locally or in a Lattice environment.
 *
 * @author Mark Fisher
 */
@Configuration
@Import(PropertyPlaceholderAutoConfiguration.class)
@ImportResource("classpath*:/META-INF/spring-xd/analytics/redis-analytics.xml")
public class ServiceConfiguration {

	@Configuration
	@Profile("cloud")
	protected static class CloudConfig extends AbstractCloudConfig {

		@Bean
		RedisConnectionFactory redisConnectionFactory() {
			return connectionFactory().redisConnectionFactory();
		}
	}

	@Configuration
	@Profile("!cloud") // TODO: enable this for cloud profile as well?
	@Import(RedisAutoConfiguration.class)
	protected static class NoCloudConfig {
	}

}
