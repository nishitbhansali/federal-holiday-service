package com.rbc.holidays.controller;

import com.rbc.holidays.util.JwtUtil;
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
 *
 * <p><b>⚠️ Production Architecture Note:</b></p>
 * <p>In production, JWT tokens are issued by a centralized Auth Service (e.g., Keycloak, Auth0, or SSO).
 * This service ONLY validates tokens — it does NOT authenticate users or issue production tokens.</p>
 *
 * <p><b>Purpose of this Controller:</b></p>
 * <p>This endpoint simulates the Auth Service for local testing when the real Auth Service
 * is not running on developer machines. It allows testing authenticated endpoints without
 * requiring full authentication infrastructure locally.</p>
 *
 * <p><b>Security:</b></p>
 * <ul>
 *   <li>Only active when {@code spring.profiles.active=local}</li>
 *   <li>Uses different {@code jwt.secret} than production</li>
 *   <li>Tokens generated here will NOT work in production</li>
 * </ul>
 *
 * @see com.rbc.holidays.filter.RequestValidationFilter
 * @see com.rbc.holidays.util.JwtUtil
 */
@RestController
@RequestMapping("/api/v1/test")
@Profile("local")
public class TestUtilityController {

    private static final Logger log = LoggerFactory.getLogger(TestUtilityController.class);

    private final JwtUtil jwtUtil;

    public TestUtilityController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates a JWT token for testing authenticated endpoints.
     *
     * @param userId the user ID to embed in the token (default: {@code test-user})
     * @return token, correlationId, userId, and expiry duration in milliseconds
     */
    @GetMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestParam(defaultValue = "test-user") String userId) {

        log.info("Generating test JWT token for userId: {}", userId);

        String token = jwtUtil.generateToken(userId);
        String correlationId = UUID.randomUUID().toString();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("correlationId", correlationId);
        response.put("userId", userId);
        response.put("expiresInMs", 3600000);

        return ResponseEntity.ok(response);
    }
}
