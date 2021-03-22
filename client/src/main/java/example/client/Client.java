package example.client;

import example.domain.Session;
import example.client.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;

import java.util.List;

import static org.apache.geode.cache.client.ClientRegionShortcut.CACHING_PROXY;

@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class) // disable subscriptions
@EnableEntityDefinedRegions(basePackageClasses = Session.class, clientRegionShortcut = CACHING_PROXY)
public class Client {

  @Autowired
  private SessionService service;

  public static void main(String[] args) {
    new SpringApplicationBuilder(Client.class)
      .build()
      .run(args);
  }

  @Bean
  ApplicationRunner runner() {
    return args -> {
      List<String> operations = args.getOptionValues("operation");
      String operation = operations.get(0);
      String parameter1 = (args.containsOption("parameter1")) ? args.getOptionValues("parameter1").get(0) : null;
      switch (operation) {
        case "create":
          this.service.create(Integer.parseInt(parameter1));
          break;
        case "add-attributes":
          this.service.addAttributes(Integer.parseInt(parameter1));
          break;
        case "remove-attributes":
          this.service.removeAttributes(Integer.parseInt(parameter1));
          break;
        case "destroy":
          this.service.destroy(Integer.parseInt(parameter1));
          break;
    }};
  }
}
