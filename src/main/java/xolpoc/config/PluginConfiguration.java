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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.xd.dirt.integration.bus.MessageBus;
import org.springframework.xd.module.core.Plugin;

import xolpoc.plugins.StreamPlugin;

/**
 * @author Dave Syer
 *
 */
@Configuration
@EnableIntegration
@Import(PropertyPlaceholderAutoConfiguration.class)
//@ImportResource({"classpath*:/META-INF/spring-xd/bus/*.xml"})
@ImportResource({ "classpath*:/META-INF/spring-xd/bus/redis-bus.xml",
		"classpath*:/META-INF/spring-xd/bus/codec.xml" })
public class PluginConfiguration {

	@Autowired
	private MessageBus messageBus;

	@Bean
	public Plugin streamPlugin() {
		return new StreamPlugin(messageBus);
	}

}
