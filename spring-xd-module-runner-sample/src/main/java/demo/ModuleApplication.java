package demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.xd.module.runner.EnableXdModule;

import config.ModuleDefinition;

@SpringBootApplication
@EnableXdModule
@ComponentScan(basePackageClasses=ModuleDefinition.class)
@PropertySource("classpath:/config/ticker.properties")
public class ModuleApplication {

	public static void main(String[] args) throws InterruptedException {
		new SpringApplicationBuilder().sources(ModuleApplication.class).run(args);
	}

}
