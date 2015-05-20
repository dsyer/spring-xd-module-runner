Experimental project allowing user to develop and run an XD module locally. Just crate `MessageChannels` "input" and/or "output" and add `@EnableXdModule` and run your app as a Spring Boot app (single application context).  You just need to connect to the physical broker for the bus, which is automatic if the relevant bus implementation is available on the classpath. The sample uses Redis.

See sample for details:

```
@SpringBootApplication
@EnableXdModule
@ImportResource("classpath:/config/ticker.xml")
@PropertySource("classpath:/config/ticker.properties")
public class ModuleApplication {

	public static void main(String[] args) throws InterruptedException {
		new SpringApplicationBuilder().sources(ModuleApplication.class).run(args);
	}

}
```

The library uses a Spring Cloud bootstrap context to initialize the `Module` environment properties in a way that simulates being deployed in a "full" XD system. The `bootstrap.yml` has the module group (a.k.a. stream name), name and index, e.g.

```
---
xd:
  config:
    home: ${XD_HOME:${xdHome:./xd}}

---
xd:
  module:
    group: testtock
    name: ${spring.application.name:ticker}
    index: ${spring.application.index:1}
```

To be deployable as an XD module in a "traditional" way you need `/config/*.properties` to point to any available Java config classes (via `base_packages` or `options_class`), or else you can put traditional XML configuration in `/config/*.xml`. You don't need those things to run as a consumer or producer to an existing XD system.