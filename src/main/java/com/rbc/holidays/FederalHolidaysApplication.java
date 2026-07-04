package com.rbc.holidays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Federal Holidays Service.
 * 
 * <p><strong>Application Overview:</strong></p>
 * <ul>
 *   <li>RESTful API for managing federal holidays (USA and CANADA)</li>
 *   <li>Enterprise-grade request validation with JWT authentication</li>
 *   <li>Correlation ID tracking for distributed tracing</li>
 *   <li>Profile-based configuration (local: H2, prod: Oracle)</li>
 * </ul>
 * 
 * <p><strong>Technology Stack:</strong></p>
 * <ul>
 *   <li>Java 21 with modern Records for DTOs</li>
 *   <li>Spring Boot 3.5.13 with auto-configuration</li>
 *   <li>Spring Data JPA with H2/Oracle support</li>
 *   <li>JWT authentication (JJWT 0.12.6)</li>
 *   <li>OpenAPI 3.0 / Swagger UI for API documentation</li>
 * </ul>
 * 
 * <p><strong>Running the Application:</strong></p>
 * <pre>
 * # Local profile (H2 in-memory database)
 * mvn spring-boot:run -Dspring-boot.run.profiles=local
 * 
 * # Production profile (Oracle database)
 * mvn spring-boot:run -Dspring-boot.run.profiles=prod \
 *   -Dspring-boot.run.arguments="--DB_URL=jdbc:oracle:thin:@//host:1521/service --JWT_SECRET=secret"
 * </pre>
 * 
 * <p><strong>Access Points:</strong></p>
 * <ul>
 *   <li>API Base: http://localhost:8080/api/v1</li>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui/index.html</li>
 *   <li>Health Check: http://localhost:8080/actuator/health</li>
 *   <li>H2 Console (local only): http://localhost:8080/h2-console</li>
 * </ul>
 * 
 * @see com.rbc.holidays.filter.RequestValidationFilter
 * @see com.rbc.holidays.config.OpenApiConfig
 */
@SpringBootApplication
public class FederalHolidaysApplication {

    /**
     * Application entry point.
     * Bootstraps Spring Boot application with auto-configuration.
     * 
     * @param args Command-line arguments (profile, properties override, etc.)
     */
    public static void main(String[] args) {
        SpringApplication.run(FederalHolidaysApplication.class, args);
    }
}
