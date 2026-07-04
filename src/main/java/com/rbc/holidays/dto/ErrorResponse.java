package com.rbc.holidays.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard error response record following RBC global exception format.
 * Uses Java Record for immutability and concise syntax.
 */
@Schema(description = "Standard error response following RBC global exception format")
public record ErrorResponse(
    
    @Schema(description = "Timestamp when the error occurred in ISO 8601 format", 
            example = "2026-07-03T10:30:00.123456-05:00")
    LocalDateTime timestamp,
    
    @Schema(description = "HTTP status code", example = "400")
    Integer status,
    
    @Schema(description = "HTTP status text", example = "Bad Request")
    String error,
    
    @Schema(description = "RBC-specific error code", example = "RBC_VALIDATION_ERROR")
    String code,
    
    @Schema(description = "Detailed error message", example = "Holiday name cannot be blank")
    String message,
    
    @Schema(description = "Request path that caused the error", example = "/api/v1/holidays")
    String path
) {}
