package demo;

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

@SpringBootApplication
public class ModuleApplication implements CommandLineRunner {

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
				.child(DeployerConfiguration.class, ModuleApplication.class)
				.properties("xd.config.home:META-INF")
			.run(args);
		// @formatter:on
	}

}
