package com.rbc.holidays.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadResponseTest {

    @Test
    void compactConstructorShouldCreateEmptyErrorsWhenNull() {
        FileUploadResponse response = new FileUploadResponse(4, 3, null);

        assertThat(response.totalRecords()).isEqualTo(4);
        assertThat(response.successCount()).isEqualTo(3);
        assertThat(response.failureCount()).isEqualTo(1);  // Calculated: 4 - 3
        assertThat(response.errors()).isEmpty();
    }

    @Test
    void compactConstructorShouldPreserveProvidedErrors() {
        FileUploadResponse response = new FileUploadResponse(
                2,
                1,
                List.of("Row 2 failed")
        );

        assertThat(response.failureCount()).isEqualTo(1);  // Calculated: 2 - 1
        assertThat(response.errors()).containsExactly("Row 2 failed");
        assertThat(response.toString()).contains("Row 2 failed");
    }
}