package com.rbc.holidays.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 Configuration for Swagger UI.
 * 
 * <p>Provides interactive API documentation at:</p>
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui/index.html</li>
 *   <li>API Docs JSON: http://localhost:8080/v3/api-docs</li>
 *   <li>API Docs YAML: http://localhost:8080/v3/api-docs.yaml</li>
 * </ul>
 * 
 * <p><strong>Security Configuration:</strong></p>
 * <ul>
 *   <li>All endpoints require JWT Bearer token authentication</li>
 *   <li>Click "Authorize" button in Swagger UI to configure bearer token</li>
 *   <li>Use {@code /api/v1/test/generate-token} endpoint to generate test tokens (local profile only)</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI specification for the Federal Holidays API.
     * 
     * <p>Defines:</p>
     * <ul>
     *   <li>API metadata (title, description, version, contact info)</li>
     *   <li>Server endpoints (local development and production)</li>
     *   <li>Security scheme (JWT Bearer authentication)</li>
     * </ul>
     * 
     * @return Configured OpenAPI instance for Swagger UI rendering
     */
    @Bean
    public OpenAPI federalHolidaysOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Federal Holidays Service API")
                        .description("RESTful API for managing Federal Holidays for USA and Canada with JWT authentication")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RBC Development Team")
                                .email("dev@rbc.com")
                                .url("https://github.com/nishitbhansali/federal-holiday-service"))
                        .license(new License()
                                .name("RBC Internal")
                                .url("https://www.rbc.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.rbc.com/federal-holidays")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token (without 'Bearer' prefix)")));
    }
}
