package cs.hse.aiclientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class AiClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiClientServiceApplication.class, args);
    }

}
