package com.rbc.holidays.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FederalHolidayTest {

    @Test
    void builderShouldPopulateFieldsAndDefaultRecurringFlag() {
        LocalDate holidayDate = LocalDate.of(2026, 7, 4);

        FederalHoliday holiday = FederalHoliday.builder()
                .id(1L)
                .holidayName("Independence Day")
                .holidayDate(holidayDate)
                .country("USA")
                .description("Federal holiday")
                .build();

        assertThat(holiday.getId()).isEqualTo(1L);
        assertThat(holiday.getHolidayName()).isEqualTo("Independence Day");
        assertThat(holiday.getHolidayDate()).isEqualTo(holidayDate);
        assertThat(holiday.getCountry()).isEqualTo("USA");
        assertThat(holiday.getIsRecurring()).isTrue();
        assertThat(holiday.getDescription()).isEqualTo("Federal holiday");
    }

    @Test
    void settersAndGettersShouldWork() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 11, 0);
        FederalHoliday holiday = new FederalHoliday();

        holiday.setId(2L);
        holiday.setHolidayName("Canada Day");
        holiday.setHolidayDate(LocalDate.of(2026, 7, 1));
        holiday.setCountry("CANADA");
        holiday.setIsRecurring(false);
        holiday.setDescription("National holiday");
        holiday.setCreatedAt(createdAt);
        holiday.setUpdatedAt(updatedAt);

        assertThat(holiday.getId()).isEqualTo(2L);
        assertThat(holiday.getHolidayName()).isEqualTo("Canada Day");
        assertThat(holiday.getHolidayDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(holiday.getCountry()).isEqualTo("CANADA");
        assertThat(holiday.getIsRecurring()).isFalse();
        assertThat(holiday.getDescription()).isEqualTo("National holiday");
        assertThat(holiday.getCreatedAt()).isEqualTo(createdAt);
        assertThat(holiday.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void allArgsConstructorEqualsHashCodeAndToStringShouldWork() {
        LocalDate holidayDate = LocalDate.of(2026, 12, 25);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 9, 0);

        FederalHoliday first = new FederalHoliday(
                3L,
                "Christmas Day",
                holidayDate,
                "USA",
                true,
                "Holiday description",
                createdAt,
                updatedAt
        );
        FederalHoliday second = new FederalHoliday(
                3L,
                "Christmas Day",
                holidayDate,
                "USA",
                true,
                "Holiday description",
                createdAt,
                updatedAt
        );

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.toString()).contains("Christmas Day", "USA");
    }
}