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

package xolpoc.bootstrap;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.xd.dirt.integration.bus.BusUtils;
import org.springframework.xd.module.ModuleDefinitions;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.module.SimpleModuleDefinition;

/**
 * @author Dave Syer
 *
 */
@ConfigurationProperties("xd.module")
public class ModuleProperties {

	private String name = "module";

	private String group = "group";

	private int index = 0;

	private String outputChannelName;

	private String inputChannelName;

	private ModuleType type = ModuleType.processor;

	private Properties consumerProperties = new Properties();

	private Properties producerProperties = new Properties();

	public SimpleModuleDefinition getModuleDefinition() {
		return ModuleDefinitions.simple(name, type, "classpath:");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getInputChannelName() {
		return (inputChannelName != null) ? inputChannelName : BusUtils
				.constructPipeName(group, index - 1);
	}

	public String getOutputChannelName() {
		return (outputChannelName != null) ? outputChannelName : BusUtils
				.constructPipeName(group, index);
	}

	public String getTapChannelName() {
		Assert.isTrue(type != ModuleType.job, "Job module type not supported.");
		// for Stream return channel name with indexed elements
		return String.format("%s.%s.%s", BusUtils.constructTapPrefix(group), name, index);
	}

	public void setOutputChannelName(String outputChannelName) {
		this.outputChannelName = outputChannelName;
	}

	public void setInputChannelName(String inputChannelName) {
		this.inputChannelName = inputChannelName;
	}

	public ModuleType getType() {
		return type;
	}

	public void setType(ModuleType type) {
		this.type = type;
	}

	public Properties getConsumerProperties() {
		return consumerProperties;
	}

	public void setConsumerProperties(Properties consumerProperties) {
		this.consumerProperties = consumerProperties;
	}

	public Properties getProducerProperties() {
		return producerProperties;
	}

	public void setProducerProperties(Properties producerProperties) {
		this.producerProperties = producerProperties;
	}

}
