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

package xolpoc.app;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.xd.module.ModuleDeploymentProperties;

import xolpoc.config.DeployerConfiguration;
import xolpoc.config.DeployerProperties;
import xolpoc.config.EmptyConfiguration;
import xolpoc.config.PluginConfiguration;
import xolpoc.config.ServiceConfiguration;
import xolpoc.core.ModuleRunner;

/**
 * Main method for running a single Module as a self-contained application.
 *
 * @author Mark Fisher
 */
@SpringBootApplication
public class ModuleBootstrap implements CommandLineRunner {

	@Autowired
	private ModuleRunner moduleRunner;

	@Autowired
	@Qualifier("moduleOptions")
	private Properties moduleOptions;

	@Autowired
	private ModuleDeploymentProperties deploymentProperties;

	@Autowired
	private DeployerProperties deployer;

	@Override
	public void run(String... args) throws Exception {
		moduleRunner.run(deployer.getModule(), moduleOptions, deploymentProperties);
	}

	public static void main(String[] args) throws InterruptedException {
		// @formatter:off	
			new SpringApplicationBuilder()
				.sources(EmptyConfiguration.class) // this hierarchical depth is expected
				.child(ServiceConfiguration.class) // so these 2 levels satisfy an assertion (temporary)
				.child(PluginConfiguration.class)
				.child(DeployerConfiguration.class, ModuleBootstrap.class)
				.properties("xd.config.home:META-INF")
			.run(args);
		// @formatter:on
	}

}
