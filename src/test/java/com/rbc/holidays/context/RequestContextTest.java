package com.rbc.holidays.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestContextTest {

    @Test
    void factoryMethodShouldDefaultPlatformToUnknown() {
        RequestContext context = RequestContext.of("123e4567-e89b-12d3-a456-426614174000", "qa-user", "/api/v1/holidays");

        assertThat(context.correlationId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(context.userId()).isEqualTo("qa-user");
        assertThat(context.platform()).isEqualTo("unknown");
        assertThat(context.requestPath()).isEqualTo("/api/v1/holidays");
    }

    @Test
    void constructorShouldRejectBlankCorrelationId() {
        assertThatThrownBy(() -> new RequestContext(" ", "qa-user", "web", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correlation ID is required");
    }

    @Test
    void constructorShouldRejectNullCorrelationId() {
        assertThatThrownBy(() -> new RequestContext(null, "qa-user", "web", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correlation ID is required");
    }

    @Test
    void constructorShouldRejectBlankUserId() {
        assertThatThrownBy(() -> new RequestContext("123e4567-e89b-12d3-a456-426614174000", " ", "web", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    void constructorShouldRejectNullUserId() {
        assertThatThrownBy(() -> new RequestContext("123e4567-e89b-12d3-a456-426614174000", null, "web", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }
}