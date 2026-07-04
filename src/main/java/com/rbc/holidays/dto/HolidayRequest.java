package com.rbc.holidays.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request record for creating or updating a federal holiday.
 * Uses Java Record for immutability and concise syntax.
 * 
 * <p>Country is validated in the service layer against configured supported countries.</p>
 */
public record HolidayRequest(
    
    @NotBlank(message = "Holiday name cannot be blank")
    String holidayName,
    
    @NotNull(message = "Holiday date cannot be null")
    LocalDate holidayDate,
    
    @NotBlank(message = "Country cannot be blank")
    String country,
    
    Boolean isRecurring,
    
    String description
) {}
