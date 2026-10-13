package com.smartreminder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI Configuration
 * 
 * Configures API documentation metadata, versioning, and global JWT Bearer
 * authentication for testing endpoints directly in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI smartReminderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Reminder REST API")
                        .description("Automated document expiration reminder system featuring JWT authentication, " +
                                "Role-Based Access Control (RBAC), and deterministic date calculation algorithms.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Reminder Team")
                                .email("support@smartreminder.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your signed JWT token obtained from POST /api/auth/login or POST /api/auth/register.")));
    }
}
