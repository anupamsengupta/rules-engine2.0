package com.quickysoft.validation.admin.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application for the Validation Engine Admin API.
 */
@SpringBootApplication(scanBasePackages = {
        "com.quickysoft.validation.admin.api",
        "com.quickysoft.validation.persistence",
        "com.quickysoft.validation.core"
})
@EntityScan(basePackages = "com.quickysoft.validation.persistence.entity")
@EnableJpaRepositories(basePackages = "com.quickysoft.validation.persistence.repository")
public class ValidationEngineAdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ValidationEngineAdminApplication.class, args);
    }
}

