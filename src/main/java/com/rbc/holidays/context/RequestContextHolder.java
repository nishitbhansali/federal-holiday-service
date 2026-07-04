package com.rbc.holidays.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal holder for RequestContext.
 * Provides thread-safe storage and retrieval of request metadata throughout the request lifecycle.
 * 
 * Design Pattern:
 * - ThreadLocal ensures each HTTP request thread has its own isolated context
 * - Filter sets context at request start, clears at request end
 * - Service/utility classes can access context without explicit parameter passing
 * 
 * Thread Safety:
 * - ThreadLocal provides thread isolation
 * - RequestContext is immutable (record)
 * - Must clear() in finally block to prevent memory leaks
 * 
 * Usage:
 * <pre>
 *   // In filter
 *   RequestContextHolder.set(new RequestContext(...));
 *   
 *   // In service/controller
 *   String correlationId = RequestContextHolder.get().correlationId();
 *   
 *   // In filter finally
 *   RequestContextHolder.clear();
 * </pre>
 */
public class RequestContextHolder {

    private static final Logger log = LoggerFactory.getLogger(RequestContextHolder.class);
    
    private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private RequestContextHolder() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
    
    /**
     * Sets the RequestContext for the current thread.
     * 
     * @param context RequestContext to store
     * @throws IllegalArgumentException if context is null
     */
    public static void set(RequestContext context) {
        if (context == null) {
            throw new IllegalArgumentException("RequestContext cannot be null");
        }
        log.trace("Setting RequestContext for correlationId: {}", context.correlationId());
        contextHolder.set(context);
    }
    
    /**
     * Retrieves the RequestContext for the current thread.
     * 
     * @return RequestContext or null if not set
     */
    public static RequestContext get() {
        return contextHolder.get();
    }
    
    /**
     * Clears the RequestContext from the current thread.
     * MUST be called in filter's finally block to prevent memory leaks.
     */
    public static void clear() {
        RequestContext context = contextHolder.get();
        if (context != null) {
            log.trace("Clearing RequestContext for correlationId: {}", context.correlationId());
        }
        contextHolder.remove();
    }
    
    /**
     * Returns the correlation ID from current context, or "UNKNOWN" if not set.
     * Useful for logging when context might not be available.
     * 
     * @return correlation ID or "UNKNOWN"
     */
    public static String getCorrelationIdOrDefault() {
        RequestContext context = get();
        return context != null ? context.correlationId() : "UNKNOWN";
    }
}
