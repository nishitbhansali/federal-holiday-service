package com.rbc.holidays.dto;

import com.rbc.holidays.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request record for creating or updating a federal holiday.
 * Uses Java Record for immutability and concise syntax.
 */
@Schema(description = "Request object for creating or updating a federal holiday")
public record HolidayRequest(
    
    @NotBlank(message = "Holiday name cannot be blank")
    @Schema(description = "Name of the federal holiday", example = "Independence Day", requiredMode = Schema.RequiredMode.REQUIRED)
    String holidayName,
    
    @NotNull(message = "Holiday date cannot be null")
    @Schema(description = "Date of the holiday in ISO 8601 format", example = "2026-07-04", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate holidayDate,
    
    @NotNull(message = "Country cannot be null")
    @Schema(description = "Country for the holiday", example = "USA", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"USA", "CANADA"})
    Country country,
    
    @Schema(description = "Indicates if the holiday recurs yearly", example = "true", defaultValue = "true")
    Boolean isRecurring,
    
    @Schema(description = "Description or significance of the holiday", example = "Celebrates the independence of the United States")
    String description
) {}
