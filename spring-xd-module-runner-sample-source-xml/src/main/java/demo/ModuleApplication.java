package demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.xd.module.runner.EnableMessageBus;

@SpringBootApplication
@EnableMessageBus
@ImportResource("classpath:/config/ticker.xml")
@PropertySource("classpath:/config/ticker.properties")
public class ModuleApplication {

	public static void main(String[] args) throws InterruptedException {
		new SpringApplicationBuilder().sources(ModuleApplication.class).run(args);
	}

}
