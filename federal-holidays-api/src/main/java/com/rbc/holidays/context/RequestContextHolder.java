package com.rbc.holidays.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal holder for request metadata (correlationId, userId, requestPath).
 * Filter sets context at request start, services access it, filter clears at end.
 */
public class RequestContextHolder {

    private static final Logger log = LoggerFactory.getLogger(RequestContextHolder.class);
    
    private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();
    
    private RequestContextHolder() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
    
    public static void set(RequestContext context) {
        if (context == null) {
            throw new IllegalArgumentException("RequestContext cannot be null");
        }
        log.trace("Setting RequestContext for correlationId: {}", context.correlationId());
        contextHolder.set(context);
    }
    
    public static RequestContext get() {
        return contextHolder.get();
    }
    
    /** Clears context - MUST be called in filter's finally block to prevent memory leaks. */
    public static void clear() {
        RequestContext context = contextHolder.get();
        if (context != null) {
            log.trace("Clearing RequestContext for correlationId: {}", context.correlationId());
        }
        contextHolder.remove();
    }
    
    public static String getCorrelationIdOrDefault() {
        RequestContext context = get();
        return context != null ? context.correlationId() : "UNKNOWN";
    }
}
