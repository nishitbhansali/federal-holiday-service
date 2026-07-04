package com.rbc.holidays.dto;

import java.time.LocalDate;

/**
 * Response record containing federal holiday details.
 * Uses Java Record for immutability and concise syntax.
 * 
 * <p>Country is returned as configured in the database (e.g., USA, CANADA, SPAIN).</p>
 */
public record HolidayResponse(
    Long id,
    String holidayName,
    LocalDate holidayDate,
    String country,
    Boolean isRecurring,
    String description
) {}
