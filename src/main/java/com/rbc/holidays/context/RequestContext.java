package com.rbc.holidays.context;

/**
 * Immutable context object holding request metadata extracted from HTTP headers.
 * This record is stored in ThreadLocal and accessible throughout the request lifecycle.
 * 
 * Design Rationale:
 * - Using Java 17+ record for immutability and conciseness
 * - Thread-safe when used with ThreadLocal
 * - Avoids passing headers through every method signature
 * - Enables correlation ID tracking in logs and exceptions
 * 
 * @param correlationId Global correlation ID (UUID format) for request tracking across services
 * @param userId        User identifier extracted from JWT token
 * @param platform      Platform identifier (web/mobile/internal-service)
 * @param requestPath   Original HTTP request path for logging/audit
 */
public record RequestContext(
        String correlationId,
        String userId,
        String platform,
        String requestPath
) {
    
    /**
     * Validates that required fields are present.
     * 
     * @throws IllegalArgumentException if any required field is null or empty
     */
    public RequestContext {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
    
    /**
     * Creates a new context with optional platform (defaults to "unknown").
     */
    public static RequestContext of(String correlationId, String userId, String requestPath) {
        return new RequestContext(correlationId, userId, "unknown", requestPath);
    }
}
