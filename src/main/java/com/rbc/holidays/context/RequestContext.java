package com.rbc.holidays.context;

/**
 * Immutable context object holding request metadata extracted from HTTP headers.
 * Stored in ThreadLocal and accessible throughout the request lifecycle.
 * 
 * @param correlationId Global correlation ID (UUID format) for request tracking
 * @param userId        User identifier extracted from JWT token
 * @param requestPath   Original HTTP request path for logging/audit
 */
public record RequestContext(
        String correlationId,
        String userId,
        String requestPath
) {
    
    public RequestContext {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
}
