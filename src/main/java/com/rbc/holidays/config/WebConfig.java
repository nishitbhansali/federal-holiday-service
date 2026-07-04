package com.rbc.holidays.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS (Cross-Origin Resource Sharing).
 * Allows browser-based clients like Swagger UI to interact with the API.
 * 
 * <p><strong>CORS Strategy:</strong></p>
 * <ul>
 *   <li><strong>Development:</strong> Uses allowedOriginPatterns("*") to permit all origins
 *       including localhost variants (needed for Swagger UI, Postman web, etc.)</li>
 *   <li><strong>Production:</strong> Should be overridden with specific allowed origins
 *       via environment-specific configuration or property override</li>
 * </ul>
 * 
 * <p><strong>Configuration Details:</strong></p>
 * <ul>
 *   <li>Applies to all endpoints (/**)</li>
 *   <li>Allows GET, POST, PUT, DELETE, OPTIONS methods</li>
 *   <li>Permits all headers (including Authorization, X-Correlation-ID)</li>
 *   <li>Allows credentials (cookies, auth headers) for authenticated requests</li>
 *   <li>Max age: 1 hour (3600 seconds) for preflight cache</li>
 * </ul>
 * 
 * <p><strong>Production Override Example:</strong></p>
 * <pre>
 * # application-prod.yml
 * spring:
 *   web:
 *     cors:
 *       allowed-origins: https://api.rbc.com,https://portal.rbc.com
 * </pre>
 * 
 * <p><strong>Why allowedOriginPatterns vs allowedOrigins:</strong></p>
 * <ul>
 *   <li>allowedOrigins("*") does NOT work with allowCredentials(true)</li>
 *   <li>allowedOriginPatterns("*") supports credentials and pattern matching</li>
 *   <li>This is required for JWT Bearer token auth in Swagger UI</li>
 * </ul>
 */
/*@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // Supports credentials, works with any origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)  // Required for Authorization header
                .maxAge(3600);  // Cache preflight response for 1 hour
    }
}*/
