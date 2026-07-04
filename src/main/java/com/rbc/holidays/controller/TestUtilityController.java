package com.rbc.holidays.controller;

import com.rbc.holidays.context.RequestContext;
import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test utilities controller for local development only.
 * Provides endpoints to generate JWT tokens and verify request context.
 * 
 * <p><b>Profile Restriction:</b> This controller is ONLY active when 
 * {@code spring.profiles.active=local}. Automatically disabled in production.</p>
 * 
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>GET /api/v1/test/generate-token - Generate JWT tokens for testing</li>
 *   <li>GET /api/v1/test/context - Verify RequestContext is working</li>
 *   <li>GET /api/v1/test/ping - Simple health check</li>
 * </ul>
 * 
 * @see com.rbc.holidays.filter.RequestValidationFilter
 * @see com.rbc.holidays.util.JwtUtil
 */
@RestController
@RequestMapping("/api/v1/test")
@Profile("local")  // Only active in local development environment
@Tag(name = "Test Utilities", description = "Development and testing utilities (local profile only)")
public class TestUtilityController {

    private static final Logger log = LoggerFactory.getLogger(TestUtilityController.class);
    
    private final JwtUtil jwtUtil;

    public TestUtilityController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates a JWT token for testing purposes.
     * 
     * @param userId User ID to embed in token (default: test-user)
     * @return JWT token and usage instructions
     */
    @GetMapping("/generate-token")
    @Operation(summary = "Generate a test JWT token", 
               description = "Creates a JWT token for testing API endpoints that require authentication")
    public ResponseEntity<Map<String, Object>> generateToken(
            @Parameter(description = "User ID for the token") 
            @RequestParam(defaultValue = "test-user") String userId) {
        
        log.info("Generating test JWT token for userId: {}", userId);
        
        String token = jwtUtil.generateToken(userId);
        String correlationId = UUID.randomUUID().toString();
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("token", token);
        response.put("authorizationHeader", "Bearer " + token);
        response.put("correlationId", correlationId);
        response.put("expiresInMs", 3600000);
        response.put("usage", Map.of(
            "X-Correlation-ID", correlationId,
            "Authorization", "Bearer " + token,
            "X-Platform", "web (optional)"
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the current request context set by the filter.
     * Useful for testing that the filter is working correctly.
     * 
     * @return Current RequestContext details
     */
    @GetMapping("/context")
    @Operation(summary = "Get current request context",
               description = "Returns the RequestContext set by RequestValidationFilter")
    public ResponseEntity<Map<String, String>> getContext() {
        
        RequestContext context = RequestContextHolder.get();
        
        if (context == null) {
            return ResponseEntity.ok(Map.of(
                "error", "No RequestContext found - filter may have bypassed this endpoint"
            ));
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("correlationId", context.correlationId());
        response.put("userId", context.userId());
        response.put("platform", context.platform());
        response.put("requestPath", context.requestPath());
        response.put("message", "Request context successfully retrieved from ThreadLocal");
        
        log.info("Request context retrieved: {}", response);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint (bypasses filter validation).
     * 
     * @return Simple status message
     */
    @GetMapping("/ping")
    @Operation(summary = "Simple ping endpoint", 
               description = "Health check that bypasses authentication")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "alive",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
