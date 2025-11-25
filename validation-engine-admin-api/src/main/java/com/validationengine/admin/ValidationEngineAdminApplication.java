package com.validationengine.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application for the Validation Engine Admin API.
 */
@SpringBootApplication(scanBasePackages = {
        "com.validationengine.admin",
        "com.validationengine.persistence",
        "com.validationengine.core"
})
@EntityScan(basePackages = "com.validationengine.persistence.entity")
@EnableJpaRepositories(basePackages = "com.validationengine.persistence.repository")
public class ValidationEngineAdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ValidationEngineAdminApplication.class, args);
    }
}

