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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Dave Syer
 */
@ConfigurationProperties("xd")
public class DeployerProperties {

	@Value("${XD_MODULE:${module:ticktock.source.time.0}}")
	private String module;

	private String input = null;

	private String output = null;

	@Value("file:${XD_HOME:${xdHome:/opt/xd}}/modules")
	private String moduleHome = "file:/opt/xd/modules";

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getModuleHome() {
		return moduleHome;
	}

	public void setModuleHome(String moduleHome) {
		this.moduleHome = moduleHome;
	}
}
