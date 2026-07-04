package com.rbc.holidays.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response record for file upload operation.
 * Uses Java Record for immutability and concise syntax.
 */
public record FileUploadResponse(
    Integer totalRecords,
    Integer successCount,
    List<String> errors
) {
    /**
     * Compact constructor with default initialization for errors list.
     */
    public FileUploadResponse {
        if (errors == null) {
            errors = new ArrayList<>();
        }
    }
    
    /**
     * Calculates failure count from total and success.
     */
    public Integer failureCount() {
        return totalRecords - successCount;
    }
}
