package com.rbc.holidays.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Utility class for JWT token generation and validation.
 * 
 * Responsibilities:
 * - Generate JWT tokens for testing/development
 * - Validate JWT token signature and expiration
 * - Extract claims (userId, roles, etc.) from tokens
 * 
 * Security Notes:
 * - Uses HMAC-SHA256 (HS256) algorithm
 * - Secret key must be at least 256 bits (32 bytes) for HS256
 * - In production, use externalized secret from Azure Key Vault or similar
 * - Tokens expire after configured duration
 * 
 * Configuration:
 * - jwt.secret: Secret key for signing (application.yml)
 * - jwt.expiration-ms: Token expiration time in milliseconds
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret:rbc-federal-holidays-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secretKey;
    
    @Value("${jwt.expiration-ms:3600000}") // Default: 1 hour
    private long expirationMs;
    
    /**
     * Generates a JWT token for the given user ID.
     * 
     * @param userId User identifier to embed in token
     * @return JWT token string
     */
    public String generateToken(String userId) {
        return generateToken(userId, Map.of());
    }
    
    /**
     * Generates a JWT token with custom claims.
     * 
     * @param userId User identifier (subject)
     * @param claims Additional claims to include
     * @return JWT token string
     */
    public String generateToken(String userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        
        log.debug("Generated JWT token for userId: {}, expiry: {}", userId, expiryDate);
        return token;
    }
    
    /**
     * Validates JWT token and returns whether it's valid.
     * 
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts user ID (subject) from JWT token.
     * 
     * @param token JWT token
     * @return User ID or null if extraction fails
     */
    public String getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts all claims from JWT token.
     * 
     * @param token JWT token
     * @return Claims object or null if extraction fails
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts token from Authorization header (removes "Bearer " prefix).
     * 
     * @param authHeader Authorization header value
     * @return JWT token string or null if invalid format
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        log.warn("Invalid Authorization header format - expected 'Bearer <token>'");
        return null;
    }
}
