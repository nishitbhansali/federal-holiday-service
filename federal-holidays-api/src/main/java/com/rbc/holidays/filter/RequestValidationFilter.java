package com.rbc.holidays.filter;

import com.rbc.holidays.context.RequestContext;
import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.UUID;

/**
 * Validates JWT/headers, initializes RequestContext, sets MDC correlation ID.
 * Runs with HIGHEST_PRECEDENCE before all other filters.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestValidationFilter.class);
    
    // Header constants
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    // MDC key for correlation ID (used in log pattern)
    private static final String MDC_CORRELATION_ID = "correlationId";
    
    // Paths that bypass authentication
    private static final String[] BYPASS_PATHS = {
        "/actuator/health",
        "/actuator/info",
        "/api/v1/test",
        "/h2-console",
        "/swagger-ui",
        "/v3/api-docs"
    };
    
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    
    public RequestValidationFilter(JwtUtil jwtUtil,
                                    HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUtil = jwtUtil;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        // Use getServletPath() to exclude context-path (/federal-holidays-api) from bypass checks
        String requestPath = request.getServletPath();
        
        try {
            // Bypass validation for health checks and API docs
            if (shouldBypass(requestPath)) {
                log.debug("Bypassing validation for path: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }
            
            // Step 1: Validate and extract correlation ID
            String correlationId = validateAndExtractCorrelationId(request);
            
            // Step 2: Set correlation ID in MDC for log tracing
            MDC.put(MDC_CORRELATION_ID, correlationId);
            
            // Step 3: Validate Authorization header and extract JWT token
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);
            if (authHeader == null || authHeader.isBlank()) {
                throw new IllegalArgumentException("Missing Authorization header");
            }
            
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Authorization header format - expected 'Bearer <token>'");
            }
            
            // Step 4: Validate JWT token
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\": \"Invalid or expired JWT token\"}");
                response.setContentType("application/json");
                return;
            }
            
            // Step 5: Extract user ID from token
            String userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("Failed to extract user ID from JWT token");
            }
            
            // Step 6: Create and set RequestContext
            RequestContext context = new RequestContext(correlationId, userId, requestPath);
            RequestContextHolder.set(context);
            
            // Step 7: Echo correlation ID back in response header for client tracking
            response.setHeader(HEADER_CORRELATION_ID, correlationId);
            
            log.debug("Request validated - Path: {}, User: {}, CorrelationId: {}", 
                    requestPath, userId, correlationId);
            
            // Step 8: Proceed with filter chain
            filterChain.doFilter(request, response);
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Request completed - Path: {}, Duration: {}ms, Status: {}", 
                    requestPath, duration, response.getStatus());
            
        } catch (Exception ex) {
            log.error("Request validation failed - Path: {}, Error: {}", requestPath, ex.getMessage());
            // Delegate to global exception handler
            handlerExceptionResolver.resolveException(request, response, null, ex);
        } finally {
            // Step 9: Clean up ThreadLocal and MDC to prevent memory leaks
            RequestContextHolder.clear();
            MDC.remove(MDC_CORRELATION_ID);
        }
    }
    
    private String validateAndExtractCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(HEADER_CORRELATION_ID);
        
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Missing X-Correlation-ID header");
        }
        
        // Validate UUID format (36 characters including hyphens)
        if (correlationId.length() != 36) {
            throw new IllegalArgumentException(
                    "Invalid X-Correlation-ID format - expected 36-character UUID");
        }
        
        // Validate UUID format
        try {
            UUID.fromString(correlationId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid X-Correlation-ID format - must be valid UUID", ex);
        }
        
        return correlationId;
    }
    
    private boolean shouldBypass(String requestPath) {
        for (String bypassPath : BYPASS_PATHS) {
            if (requestPath.startsWith(bypassPath)) {
                return true;
            }
        }
        return false;
    }
}
