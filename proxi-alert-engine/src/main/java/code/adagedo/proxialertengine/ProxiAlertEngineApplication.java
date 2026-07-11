package code.adagedo.proxialertengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxiAlertEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxiAlertEngineApplication.class, args);
    }

}
