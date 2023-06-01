package cs.hse.scansprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("cs.hse.scansprovider")
public class ScansProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScansProviderApplication.class, args);
    }

}
