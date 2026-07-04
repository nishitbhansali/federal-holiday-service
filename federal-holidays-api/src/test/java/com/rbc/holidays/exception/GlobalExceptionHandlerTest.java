package com.rbc.holidays.exception;

import com.rbc.holidays.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/holidays");
    }

    @Test
    void handleIllegalArgumentExceptionShouldReturnBadRequest() {
        var response = handler.handleIllegalArgumentException(new IllegalArgumentException("bad input"), request);

        assertError(response.getBody(), HttpStatus.BAD_REQUEST, "RBC_VALIDATION_ERROR", "bad input");
    }

    @Test
    void handleHolidayNotFoundExceptionShouldReturnNotFound() {
        var response = handler.handleHolidayNotFoundException(new HolidayNotFoundException(99L), request);

        assertError(response.getBody(), HttpStatus.NOT_FOUND, "RBC_HOLIDAY_NOT_FOUND", "Holiday not found with id: 99");
    }

    @Test
    void handleDuplicateHolidayExceptionShouldReturnConflict() {
        var response = handler.handleDuplicateHolidayException(new DuplicateHolidayException("duplicate"), request);

        assertError(response.getBody(), HttpStatus.CONFLICT, "RBC_DUPLICATE_HOLIDAY", "duplicate");
    }

    @Test
    void handleInvalidCountryExceptionShouldReturnBadRequest() {
        var response = handler.handleInvalidCountryException(new InvalidCountryException("MARS", "USA, CANADA"), request);

        assertError(response.getBody(), HttpStatus.BAD_REQUEST, "RBC_INVALID_COUNTRY", "Invalid country 'MARS'. Supported countries: USA, CANADA");
    }

    @Test
    void handleInvalidFileFormatExceptionShouldReturnBadRequest() {
        var response = handler.handleInvalidFileFormatException(new InvalidFileFormatException("bad file"), request);

        assertError(response.getBody(), HttpStatus.BAD_REQUEST, "RBC_INVALID_FILE_FORMAT", "bad file");
    }

    @Test
    void handleValidationExceptionShouldAggregateFieldMessages() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ValidationTarget(), "validationTarget");
        bindingResult.addError(new FieldError("validationTarget", "country", "Country required"));
        bindingResult.addError(new FieldError("validationTarget", "holidayDate", "Date required"));

        Method method = ValidationTarget.class.getDeclaredMethod("validate", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidationException(exception, request);

        assertError(response.getBody(), HttpStatus.BAD_REQUEST, "RBC_VALIDATION_ERROR", "Country required, Date required");
    }

    @Test
    void handleDataIntegrityViolationExceptionShouldReturnConflict() {
        var response = handler.handleDataIntegrityViolationException(new DataIntegrityViolationException("duplicate key"), request);

        assertError(response.getBody(), HttpStatus.CONFLICT, "RBC_DATA_INTEGRITY_VIOLATION", "A holiday already exists for this country on the specified date");
    }

    @Test
    void handleMaxUploadSizeExceededExceptionShouldReturnPayloadTooLarge() {
        var response = handler.handleMaxUploadSizeExceededException(new MaxUploadSizeExceededException(10_000_000), request);

        assertError(response.getBody(), HttpStatus.PAYLOAD_TOO_LARGE, "RBC_FILE_SIZE_EXCEEDED", "File size exceeds the maximum allowed limit of 10MB");
    }

    @Test
    void handleGlobalExceptionShouldReturnInternalServerError() {
        var response = handler.handleGlobalException(new RuntimeException("boom"), request);

        assertError(response.getBody(), HttpStatus.INTERNAL_SERVER_ERROR, "RBC_GLOBAL_EXCEPTION", "An unexpected error occurred. Please contact support.");
    }

    private void assertError(ErrorResponse body, HttpStatus status, String code, String message) {
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(status.value());
        assertThat(body.error()).isEqualTo(status.getReasonPhrase());
        assertThat(body.code()).isEqualTo(code);
        assertThat(body.message()).isEqualTo(message);
        assertThat(body.path()).isEqualTo("/api/v1/holidays");
        assertThat(body.timestamp()).isNotNull();
    }

    static class ValidationTarget {
        void validate(String field) {
        }
    }
}