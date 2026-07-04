package com.rbc.holidays.dto;

import com.rbc.holidays.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating or updating a federal holiday")
public class HolidayRequest {

    @NotBlank(message = "Holiday name cannot be blank")
    @Schema(description = "Name of the federal holiday", example = "Independence Day", required = true)
    private String holidayName;

    @NotNull(message = "Holiday date cannot be null")
    @Schema(description = "Date of the holiday in ISO 8601 format", example = "2026-07-04", required = true)
    private LocalDate holidayDate;

    @NotNull(message = "Country cannot be null")
    @Schema(description = "Country for the holiday", example = "USA", required = true, allowableValues = {"USA", "CANADA"})
    private Country country;

    @Schema(description = "Indicates if the holiday recurs yearly", example = "true", defaultValue = "true")
    private Boolean isRecurring;

    @Schema(description = "Description or significance of the holiday", example = "Celebrates the independence of the United States")
    private String description;
}
