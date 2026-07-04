package com.rbc.holidays.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Response record for file upload operation.
 * Uses Java Record for immutability and concise syntax.
 */
@Schema(description = "Response object for file upload operation")
public record FileUploadResponse(
    
    @Schema(description = "Total number of records in the uploaded file", example = "10")
    Integer totalRecords,
    
    @Schema(description = "Number of successfully processed records", example = "8")
    Integer successCount,
    
    @Schema(description = "Number of failed records", example = "2")
    Integer failureCount,
    
    @Schema(description = "List of error messages for failed records")
    List<String> errors,
    
    @Schema(description = "Overall upload status message", example = "File uploaded successfully with 8 records")
    String message
) {
    /**
     * Compact constructor with default initialization for errors list.
     */
    public FileUploadResponse {
        if (errors == null) {
            errors = new ArrayList<>();
        }
    }
}
