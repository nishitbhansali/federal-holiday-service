package com.rbc.holidays.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard error response following RBC global exception format")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred in ISO 8601 format", 
            example = "2026-07-03T10:30:00.123456-05:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "HTTP status text", example = "Bad Request")
    private String error;

    @Schema(description = "RBC-specific error code", example = "RBC_VALIDATION_ERROR")
    private String code;

    @Schema(description = "Detailed error message", example = "Holiday name cannot be blank")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/holidays")
    private String path;
}
