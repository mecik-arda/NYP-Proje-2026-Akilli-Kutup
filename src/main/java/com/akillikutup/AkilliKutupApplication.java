package com.akillikutup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = {"com.akillikutup.user", "com.akillikutup.material"})
public class AkilliKutupApplication {

    public static void main(String[] args) {
        SpringApplication.run(AkilliKutupApplication.class, args);
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  AKILLI KÜTÜPHANE V4.2 - Spring Boot + PostgreSQL");
        System.out.println("  http://localhost:8080");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
