package com.rbc.holidays.exception;

import com.rbc.holidays.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

/**
 * Global exception handler for all controllers.
 * Provides centralized exception handling and consistent error responses.
 * 
 * Design Pattern:
 * - Uses @RestControllerAdvice for global exception handling
 * - Returns standardized ErrorResponse for all exceptions
 * - Logs all exceptions for monitoring and debugging
 * - Integrates with RequestValidationFilter via HandlerExceptionResolver
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles request validation errors from filter layer.
     * Triggered when required headers are missing or invalid (correlationId, Authorization).
     * 
     * @param ex IllegalArgumentException with validation error details
     * @param request Current HTTP request
     * @return 400 BAD REQUEST with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.error("Request validation error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "RBC_VALIDATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles holiday not found exceptions.
     * Triggered when attempting to get, update, or delete a non-existent holiday.
     * 
     * @param ex HolidayNotFoundException with holiday ID details
     * @param request Current HTTP request
     * @return 404 NOT FOUND with error details
     */
    @ExceptionHandler(HolidayNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHolidayNotFoundException(
            HolidayNotFoundException ex, HttpServletRequest request) {
        logger.error("Holiday not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "RBC_HOLIDAY_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles duplicate holiday exceptions.
     * Triggered when creating/updating a holiday that already exists for the country and date.
     * 
     * @param ex DuplicateHolidayException with conflict details
     * @param request Current HTTP request
     * @return 409 CONFLICT with error details
     */
    @ExceptionHandler(DuplicateHolidayException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateHolidayException(
            DuplicateHolidayException ex, HttpServletRequest request) {
        logger.error("Duplicate holiday: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "RBC_DUPLICATE_HOLIDAY",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles invalid country exceptions.
     * Triggered when a country code is not in the supported countries list.
     * 
     * @param ex InvalidCountryException with country details
     * @param request Current HTTP request
     * @return 400 BAD REQUEST with error details
     */
    @ExceptionHandler(InvalidCountryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCountryException(
            InvalidCountryException ex, HttpServletRequest request) {
        logger.error("Invalid country: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "RBC_INVALID_COUNTRY",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles invalid file format exceptions during file upload.
     * Triggered when uploaded file is empty, unsupported type, or malformed.
     * 
     * @param ex InvalidFileFormatException with format error details
     * @param request Current HTTP request
     * @return 400 BAD REQUEST with error details
     */
    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileFormatException(
            InvalidFileFormatException ex, HttpServletRequest request) {
        logger.error("Invalid file format: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "RBC_INVALID_FILE_FORMAT",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles bean validation exceptions.
     * Triggered when @Valid annotated request body fails validation (@NotNull, @NotBlank, etc.).
     * Aggregates all field errors into a single error message.
     * 
     * @param ex MethodArgumentNotValidException with field validation errors
     * @param request Current HTTP request
     * @return 400 BAD REQUEST with aggregated validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.error("Validation error: {}", ex.getMessage());
        
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "RBC_VALIDATION_ERROR",
                message,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles database constraint violations.
     * Triggered when database unique constraint fails (duplicate country + date).
     * Provides user-friendly message instead of technical database error.
     * 
     * @param ex DataIntegrityViolationException from database layer
     * @param request Current HTTP request
     * @return 409 CONFLICT with user-friendly error message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "A holiday already exists for this country on the specified date";
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "RBC_DATA_INTEGRITY_VIOLATION",
                message,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles file upload size limit exceptions.
     * Triggered when uploaded file exceeds configured maximum size (10MB).
     * 
     * @param ex MaxUploadSizeExceededException from Spring multipart
     * @param request Current HTTP request
     * @return 413 PAYLOAD TOO LARGE with size limit details
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.error("File size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
                "RBC_FILE_SIZE_EXCEEDED",
                "File size exceeds the maximum allowed limit of 10MB",
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handles all unhandled exceptions as fallback.
     * Catches any exception not handled by specific handlers above.
     * Returns generic error message to avoid leaking sensitive system details.
     * 
     * @param ex Any uncaught exception
     * @param request Current HTTP request
     * @return 500 INTERNAL SERVER ERROR with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: ", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.of("America/Toronto")),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "RBC_GLOBAL_EXCEPTION",
                "An unexpected error occurred. Please contact support.",
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
