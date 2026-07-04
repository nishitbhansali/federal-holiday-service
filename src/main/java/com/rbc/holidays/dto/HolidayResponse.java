package com.rbc.holidays.dto;

import com.rbc.holidays.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing federal holiday details")
public class HolidayResponse {

    @Schema(description = "Unique identifier of the holiday", example = "1")
    private Long id;

    @Schema(description = "Name of the federal holiday", example = "Independence Day")
    private String holidayName;

    @Schema(description = "Date of the holiday in ISO 8601 format", example = "2026-07-04")
    private LocalDate holidayDate;

    @Schema(description = "Country for the holiday", example = "USA")
    private Country country;

    @Schema(description = "Indicates if the holiday recurs yearly", example = "true")
    private Boolean isRecurring;

    @Schema(description = "Description or significance of the holiday", example = "Celebrates the independence of the United States")
    private String description;

    @Schema(description = "Timestamp when the holiday was created", example = "2026-07-03T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the holiday was last updated", example = "2026-07-03T10:30:00")
    private LocalDateTime updatedAt;
}
