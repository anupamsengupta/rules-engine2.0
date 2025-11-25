package com.quickysoft.validation.admin.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI validationEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Validation Engine Admin API")
                        .description("REST API for managing rule sets and rules in a multi-tenant validation engine")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Validation Engine Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production server")
                ));
    }
}

