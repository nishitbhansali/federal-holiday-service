package com.rbc.holidays.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayResponseTest {

    @Test
    void recordShouldExposeAllValues() {
        HolidayResponse response = new HolidayResponse(
                10L,
                "Canada Day",
                LocalDate.of(2026, 7, 1),
                "CANADA",
                true,
                "National holiday"
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.holidayName()).isEqualTo("Canada Day");
        assertThat(response.holidayDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(response.country()).isEqualTo("CANADA");
        assertThat(response.isRecurring()).isTrue();
        assertThat(response.description()).isEqualTo("National holiday");
        assertThat(response.toString()).contains("Canada Day", "CANADA");
    }
}