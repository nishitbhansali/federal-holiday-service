package com.rbc.holidays.context;

/**
 * Immutable request metadata (correlationId, userId, requestPath) stored in ThreadLocal.
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
