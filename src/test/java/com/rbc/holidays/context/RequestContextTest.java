package com.rbc.holidays.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestContextTest {

    @Test
    void constructorShouldCreateValidContext() {
        RequestContext context = new RequestContext("123e4567-e89b-12d3-a456-426614174000", "qa-user", "/api/v1/holidays");

        assertThat(context.correlationId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(context.userId()).isEqualTo("qa-user");
        assertThat(context.requestPath()).isEqualTo("/api/v1/holidays");
    }

    @Test
    void constructorShouldRejectBlankCorrelationId() {
        assertThatThrownBy(() -> new RequestContext(" ", "qa-user", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correlation ID is required");
    }

    @Test
    void constructorShouldRejectNullCorrelationId() {
        assertThatThrownBy(() -> new RequestContext(null, "qa-user", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correlation ID is required");
    }

    @Test
    void constructorShouldRejectBlankUserId() {
        assertThatThrownBy(() -> new RequestContext("123e4567-e89b-12d3-a456-426614174000", " ", "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    void constructorShouldRejectNullUserId() {
        assertThatThrownBy(() -> new RequestContext("123e4567-e89b-12d3-a456-426614174000", null, "/path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }
}