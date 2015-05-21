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
    index: 0 # source
```

To be deployable as an XD module in a "traditional" way you need `/config/*.properties` to point to any available Java config classes (via `base_packages` or `options_class`), or else you can put traditional XML configuration in `/config/*.xml`. You don't need those things to run as a consumer or producer to an existing XD system.

## Module or App

Code using this library can be deployed as a standlaone app or as an XD module. In standalone mode you app will run happily as a service or in any PaaS (Cloud Foundry, Lattice, Heroku, Azure, etc.). Depending on whether your main aim is to develop an XD module and you just want to test it locally using the standalone mode, or if the ultimate goal is a standalone app, there are some things that you might do differently.

### Module Options

Module option (placeholders) default values can be set in `/config/*.properties` as per a normal XD module, and they can be overridden at runtime in standalone mode using standard Spring Boot configuration (e.g. `application.yml`). Because of the way XD likes to organize options, the default values can also be set as `option.*` in `bootstrap.yml` (in standalone mode) or as System properties (generally).

## Local Configuration

The `application.yml` and `bootstrap.yml` files are ignored by XD when deploying the module natively, so you can put whatever you like in there to control the app in standlone mode.

## Making Standalone Modules Talk to Each Other

The "group" and "index" are used to create physical endpoints in the external broker (e.g. `queue.<group>.<index>` in Redis), so a source (output only) has `index=0` (the default) and downstream modules have the same group but incremented index, with a sink module (input only) has the highest index. To listen to the output from an existing app, just use the same "group" and an index 1 larger than it has.

All output channels are also tapped by default so you can also attach a module to a pub-sub endpoint and listen to the tap if you know the module metadata (e.g. `topic.tap:stream:<name>.<group>.<index>` in Redis). TODO: configure tap metadata so that you can automatically listen to an existing tap.


