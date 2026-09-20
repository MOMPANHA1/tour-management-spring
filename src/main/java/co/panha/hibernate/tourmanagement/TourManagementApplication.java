package co.panha.hibernate.tourmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @EnableScheduling} ចាំបាច់សម្រាប់ {@code @Scheduled} នៅ
 * {@code ScheduleServiceImpl.refreshScheduleStatuses()} — បើគ្មានវា job នឹង<b>មិនរត់ទេ</b>
 * ដោយគ្មានកំហុសអ្វីបង្ហាញ។
 */
@EnableScheduling
@SpringBootApplication
public class TourManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourManagementApplication.class, args);
    }

}
