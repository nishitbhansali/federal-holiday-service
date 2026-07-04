package com.rbc.holidays.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayRequestTest {

    @Test
    void recordShouldExposeAllValues() {
        HolidayRequest request = new HolidayRequest(
                "Independence Day",
                LocalDate.of(2026, 7, 4),
                "USA",
                true,
                "Federal holiday"
        );

        assertThat(request.holidayName()).isEqualTo("Independence Day");
        assertThat(request.holidayDate()).isEqualTo(LocalDate.of(2026, 7, 4));
        assertThat(request.country()).isEqualTo("USA");
        assertThat(request.isRecurring()).isTrue();
        assertThat(request.description()).isEqualTo("Federal holiday");
        assertThat(request.toString()).contains("Independence Day", "USA");
    }
}