package com.rbc.holidays.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void recordShouldExposeAllValues() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 4, 12, 30);
        ErrorResponse response = new ErrorResponse(
                timestamp,
                400,
                "Bad Request",
                "RBC_VALIDATION_ERROR",
                "Validation failed",
                "/api/v1/holidays"
        );

        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.code()).isEqualTo("RBC_VALIDATION_ERROR");
        assertThat(response.message()).isEqualTo("Validation failed");
        assertThat(response.path()).isEqualTo("/api/v1/holidays");
        assertThat(response.toString()).contains("RBC_VALIDATION_ERROR", "/api/v1/holidays");
    }
}