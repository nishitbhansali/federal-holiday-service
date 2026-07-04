package com.rbc.holidays.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTest {

    @Test
    void duplicateHolidayExceptionShouldStoreMessage() {
        DuplicateHolidayException exception = new DuplicateHolidayException("duplicate holiday");

        assertThat(exception).hasMessage("duplicate holiday");
    }

    @Test
    void holidayNotFoundExceptionShouldSupportBothConstructors() {
        HolidayNotFoundException withMessage = new HolidayNotFoundException("missing holiday");
        HolidayNotFoundException withId = new HolidayNotFoundException(55L);

        assertThat(withMessage).hasMessage("missing holiday");
        assertThat(withId).hasMessage("Holiday not found with id: 55");
    }

    @Test
    void invalidCountryExceptionShouldFormatMessage() {
        InvalidCountryException exception = new InvalidCountryException("MARS", "USA, CANADA");

        assertThat(exception).hasMessage("Invalid country 'MARS'. Supported countries: USA, CANADA");
    }

    @Test
    void invalidFileFormatExceptionShouldSupportCause() {
        IllegalArgumentException cause = new IllegalArgumentException("bad format");
        InvalidFileFormatException withMessage = new InvalidFileFormatException("invalid file");
        InvalidFileFormatException withCause = new InvalidFileFormatException("invalid file", cause);

        assertThat(withMessage).hasMessage("invalid file");
        assertThat(withCause).hasMessage("invalid file");
        assertThat(withCause.getCause()).isSameAs(cause);
    }
}