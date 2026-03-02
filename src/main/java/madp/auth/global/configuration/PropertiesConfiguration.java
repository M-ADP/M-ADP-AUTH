package madp.auth.global.configuration;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackages = "madp.auth.global.properties")
public class PropertiesConfiguration {
}