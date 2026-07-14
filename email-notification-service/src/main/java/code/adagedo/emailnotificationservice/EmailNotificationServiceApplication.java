package code.adagedo.emailnotificationservice;

import code.adagedo.emailnotificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmailNotificationServiceApplication {

//    @Autowired
//    EmailService emailService;
    public static void main(String[] args) {
        SpringApplication.run(EmailNotificationServiceApplication.class, args);
    }



    // test code snippet
//    @Bean
//    CommandLineRunner commandLineRunner() {
//        return args -> emailService.sendMail("johnnanfanali@gmail.com", "Welcome Email", "Welcome to proxiSentry");
//    }
}

