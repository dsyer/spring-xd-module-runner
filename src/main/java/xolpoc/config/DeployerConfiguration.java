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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.xd.dirt.module.ModuleDeployer;
import org.springframework.xd.dirt.module.ModuleRegistry;
import org.springframework.xd.dirt.module.ResourceModuleRegistry;
import org.springframework.xd.dirt.plugins.job.JobPluginMetadataResolver;
import org.springframework.xd.dirt.plugins.stream.ModuleTypeConversionPluginMetadataResolver;
import org.springframework.xd.module.ModuleDeploymentProperties;
import org.springframework.xd.module.core.ModuleFactory;
import org.springframework.xd.module.options.DefaultModuleOptionsMetadataResolver;
import org.springframework.xd.module.options.DelegatingModuleOptionsMetadataResolver;
import org.springframework.xd.module.options.EnvironmentAwareModuleOptionsMetadataResolver;
import org.springframework.xd.module.options.ModuleOptionsMetadataResolver;

import xolpoc.core.ModuleRunner;

/**
 * Instantiates the components required for loading and deploying Modules.
 *
 * @author Mark Fisher
 */
@Configuration
@EnableConfigurationProperties(DeployerProperties.class)
public class DeployerConfiguration {
	
	@Autowired
	private DeployerProperties deployer;

	@Bean
	public ModuleRunner moduleRunner(ModuleRegistry moduleRegistry,
			ModuleDeployer moduleDeployer) {
		return new ModuleRunner(moduleRegistry, moduleDeployer, deployer.getInput(), deployer.getOutput());
	}

	@Bean
	public ModuleDeployer moduleDeployer() {
		return new ModuleDeployer(moduleFactory());
	}

	@Bean
	public ResourceModuleRegistry moduleRegistry() {
		return new ResourceModuleRegistry(deployer.getModuleHome());
	}

	@Bean
	public ModuleFactory moduleFactory() {
		return new ModuleFactory(moduleOptionsMetadataResolver());
	}
	
	@Bean
	public ModuleDeploymentProperties moduleDeploymentProperties(Environment environment) {
		ModuleDeploymentProperties deploymentProperties = new ModuleDeploymentProperties();
		Map<String, Object> map = new RelaxedPropertyResolver(environment).getSubProperties("property.");
		for (String key : map.keySet()) {
			Object value = map.get(key);
			deploymentProperties.put(key, value==null ? "null" : value.toString());
		}
		return deploymentProperties;
	}

	@Bean
	public Properties moduleOptions(Environment environmemt) {
		Map<String, Object> map = new RelaxedPropertyResolver(environmemt).getSubProperties("option.");
		Properties properties = new Properties();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			properties.setProperty(key, value==null ? "null" : value.toString());
		}
		return properties;
	}

	@Bean
	public EnvironmentAwareModuleOptionsMetadataResolver moduleOptionsMetadataResolver() {
		List<ModuleOptionsMetadataResolver> delegates = new ArrayList<ModuleOptionsMetadataResolver>();
		delegates.add(defaultModuleOptionsMetadataResolver());
		delegates.add(new ModuleTypeConversionPluginMetadataResolver());
		delegates.add(new JobPluginMetadataResolver());
		DelegatingModuleOptionsMetadataResolver delegatingResolver = new DelegatingModuleOptionsMetadataResolver();
		delegatingResolver.setDelegates(delegates);
		EnvironmentAwareModuleOptionsMetadataResolver resolver = new EnvironmentAwareModuleOptionsMetadataResolver();
		resolver.setDelegate(delegatingResolver);
		return resolver;
	}

	@Bean
	public DefaultModuleOptionsMetadataResolver defaultModuleOptionsMetadataResolver() {
		DefaultModuleOptionsMetadataResolver resolver = new DefaultModuleOptionsMetadataResolver();
		//resolver.setCompositeResolver(moduleOptionsMetadataResolver());
		return resolver;
	}

}
