package com.rbc.holidays.filter;

import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestValidationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
        MDC.clear();
    }

    @Test
    void shouldBypassConfiguredPaths() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    void shouldDelegateWhenCorrelationIdHeaderMissing() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/v1/holidays");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing X-Correlation-ID header");
    }

    @Test
    void shouldDelegateWhenCorrelationIdLengthIsInvalid() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = baseRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "short-id");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid X-Correlation-ID format - expected 36-character UUID");
    }

    @Test
    void shouldDelegateWhenCorrelationIdIsNotValidUuid() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = baseRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "123e4567-e89b-12d3-a456-42661417400Z");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid X-Correlation-ID format - must be valid UUID");
    }

    @Test
    void shouldDelegateWhenAuthorizationHeaderMissing() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = baseRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "123e4567-e89b-12d3-a456-426614174000");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing Authorization header");
    }

    @Test
    void shouldDelegateWhenAuthorizationHeaderFormatIsInvalid() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = baseRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "123e4567-e89b-12d3-a456-426614174000");
        request.addHeader("Authorization", "Token abc");
        when(jwtUtil.extractTokenFromHeader("Token abc")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Authorization header format - expected 'Bearer <token>'");
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = authenticatedRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.extractTokenFromHeader("Bearer jwt-token")).thenReturn("jwt-token");
        when(jwtUtil.validateToken("jwt-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isEqualTo("{\"error\": \"Invalid or expired JWT token\"}");
        assertThat(response.getContentType()).isEqualTo("application/json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldDelegateWhenUserIdCannotBeExtracted() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = authenticatedRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.extractTokenFromHeader("Bearer jwt-token")).thenReturn("jwt-token");
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn(" ");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to extract user ID from JWT token");
    }

    @Test
    void shouldSetAndClearContextForValidRequest() throws Exception {
        RequestValidationFilter filter = new RequestValidationFilter(jwtUtil, handlerExceptionResolver);
        MockHttpServletRequest request = authenticatedRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.extractTokenFromHeader("Bearer jwt-token")).thenReturn("jwt-token");
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("qa-user");
        doAnswer(invocation -> {
            assertThat(RequestContextHolder.get()).isNotNull();
            assertThat(RequestContextHolder.get().correlationId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
            assertThat(RequestContextHolder.get().userId()).isEqualTo("qa-user");
            assertThat(RequestContextHolder.get().platform()).isEqualTo("unknown");
            assertThat(RequestContextHolder.get().requestPath()).isEqualTo("/api/v1/holidays");
            assertThat(MDC.get("correlationId")).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(RequestContextHolder.get()).isNull();
        assertThat(MDC.get("correlationId")).isNull();
    }

    private MockHttpServletRequest baseRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/holidays");
        return request;
    }

    private MockHttpServletRequest authenticatedRequest() {
        MockHttpServletRequest request = baseRequest();
        request.addHeader("X-Correlation-ID", "123e4567-e89b-12d3-a456-426614174000");
        request.addHeader("Authorization", "Bearer jwt-token");
        return request;
    }
}