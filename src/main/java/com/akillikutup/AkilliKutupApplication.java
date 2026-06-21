package com.akillikutup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.akillikutup.repository")
public class AkilliKutupApplication {

    public static void main(String[] args) {
        SpringApplication.run(AkilliKutupApplication.class, args);
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  AKILLI KÜTÜPHANE V4.1 — Spring Boot + PostgreSQL");
        System.out.println("  http://localhost:8080");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
