package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.xd.module.runner.EnableXdModule;

import config.ModuleDefinition;

@SpringBootApplication
@EnableXdModule
@ComponentScan(basePackageClasses=ModuleDefinition.class)
public class SourceApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SourceApplication.class, args);
	}

}
