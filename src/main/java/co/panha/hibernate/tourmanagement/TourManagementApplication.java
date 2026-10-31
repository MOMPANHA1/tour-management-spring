package co.panha.hibernate.tourmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling is required for @Scheduled jobs; without it they silently never run.
@EnableScheduling
@SpringBootApplication
public class TourManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourManagementApplication.class, args);
    }

}
