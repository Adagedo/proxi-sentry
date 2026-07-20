package code.adagedo.smsnotificationservice;

import code.adagedo.smsnotificationservice.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SmsNotificationServiceApplication {

//    @Autowired
//    SmsService smsService;
    public static void main(String[] args) {
        SpringApplication.run(SmsNotificationServiceApplication.class, args);
    }

//      @Bean
//      CommandLineRunner commandLineRunner() {
//       return args -> smsService.sendMsm();
//   }
}
