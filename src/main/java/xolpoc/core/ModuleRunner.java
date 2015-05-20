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

package xolpoc.core;

import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.xd.dirt.module.ModuleDeployer;
import org.springframework.xd.dirt.module.ModuleRegistry;
import org.springframework.xd.module.ModuleDefinition;
import org.springframework.xd.module.ModuleDeploymentProperties;
import org.springframework.xd.module.ModuleDescriptor;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.module.core.Module;

/**
 * @author Mark Fisher
 */
public class ModuleRunner {

	private final ModuleRegistry registry;

	private final ModuleDeployer deployer;

	private final String input;

	private final String output;

	public ModuleRunner(ModuleRegistry registry, ModuleDeployer deployer, String input,
			String output) {
		Assert.notNull(registry, "ModuleRegistry must not be null");
		Assert.notNull(deployer, "ModuleDeployer must not be null");
		this.input = input;
		this.output = output;
		this.registry = registry;
		this.deployer = deployer;
	}

	public void run(String moduleDefinition, Properties moduleOptions,
			ModuleDeploymentProperties deploymentProperties) {
		deploy(descriptorFor(moduleDefinition, moduleOptions), deploymentProperties);
	}

	private ModuleDescriptor descriptorFor(String moduleDefinition, Properties options) {
		String[] tokens = moduleDefinition.split("\\.");
		Assert.isTrue(tokens.length == 4,
				"required module property format: streamname.moduletype.modulename.moduleindex");
		String streamName = tokens[0];
		ModuleType moduleType = ModuleType.valueOf(tokens[1]);
		String moduleName = tokens[2];
		int moduleIndex = Integer.parseInt(tokens[3]);

		ModuleDefinition definition = registry.findDefinition(moduleName, moduleType);

		ModuleDescriptor.Builder builder = new ModuleDescriptor.Builder()
				.setModuleDefinition(definition).setModuleName(moduleName)
				.setType(moduleType).setGroup(streamName).setIndex(moduleIndex);
		builder.setSourceChannelName(input);
		builder.setSinkChannelName(output);
		for (String key : options.stringPropertyNames()) {
			builder.setParameter(key, options.getProperty(key));
		}
		return builder.build();
	}

	private void deploy(ModuleDescriptor descriptor,
			ModuleDeploymentProperties deploymentProperties) {
		Module module = deployer.createModule(descriptor, deploymentProperties);
		deployer.deploy(module, descriptor);
	}

}
