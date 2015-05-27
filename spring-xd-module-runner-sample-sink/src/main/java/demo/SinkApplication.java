package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.xd.module.runner.EnableMessageBus;

import config.ModuleDefinition;

@SpringBootApplication
@EnableMessageBus
@ComponentScan(basePackageClasses=ModuleDefinition.class)
public class SinkApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SinkApplication.class, args);
	}

}
