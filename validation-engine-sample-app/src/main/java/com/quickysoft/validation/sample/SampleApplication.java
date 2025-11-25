package com.quickysoft.validation.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Sample Spring Boot application demonstrating validation engine usage.
 */
@SpringBootApplication(scanBasePackages = {
        "com.quickysoft.validation.sample",
        "com.quickysoft.validation.persistence",
        "com.quickysoft.validation.core"
})
@EntityScan(basePackages = "com.quickysoft.validation.persistence.entity")
@EnableJpaRepositories(basePackages = "com.quickysoft.validation.persistence.repository")
public class SampleApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}

