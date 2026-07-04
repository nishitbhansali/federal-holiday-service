package com.rbc.holidays.dto;

import com.rbc.holidays.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response record containing federal holiday details.
 * Uses Java Record for immutability and concise syntax.
 */
@Schema(description = "Response object containing federal holiday details")
public record HolidayResponse(
    
    @Schema(description = "Unique identifier of the holiday", example = "1")
    Long id,
    
    @Schema(description = "Name of the federal holiday", example = "Independence Day")
    String holidayName,
    
    @Schema(description = "Date of the holiday in ISO 8601 format", example = "2026-07-04")
    LocalDate holidayDate,
    
    @Schema(description = "Country for the holiday", example = "USA")
    Country country,
    
    @Schema(description = "Indicates if the holiday recurs yearly", example = "true")
    Boolean isRecurring,
    
    @Schema(description = "Description or significance of the holiday", example = "Celebrates the independence of the United States")
    String description,
    
    @Schema(description = "Timestamp when the holiday was created", example = "2026-07-03T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "Timestamp when the holiday was last updated", example = "2026-07-03T10:30:00")
    LocalDateTime updatedAt
) {}
