package com.rbc.holidays.dto;

import java.time.LocalDateTime;

/**
 * Standard error response record following RBC global exception format.
 * Uses Java Record for immutability and concise syntax.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String code,
    String message,
    String path
) {}
