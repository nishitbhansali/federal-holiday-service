package com.rbc.holidays.controller;

import com.rbc.holidays.context.RequestContext;
import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.dto.ErrorResponse;
import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.enums.Country;
import com.rbc.holidays.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Federal Holidays management.
 * Handles HTTP requests for CRUD operations on USA and Canada federal holidays.
 * 
 * <p><b>Controller Responsibilities (HTTP Concerns Only):</b></p>
 * <ul>
 *   <li>Map HTTP requests to service layer methods</li>
 *   <li>Validate request payloads via @Valid</li>
 *   <li>Return type-safe HTTP responses</li>
 *   <li>Log operations with user context for audit trail</li>
 * </ul>
 * 
 * <p><b>Design Pattern - Enterprise Filter Architecture:</b></p>
 * <ul>
 *   <li><b>RequestValidationFilter</b> handles all header validation and authentication</li>
 *   <li><b>Required headers:</b> X-Correlation-ID (UUID), Authorization (Bearer JWT)</li>
 *   <li><b>RequestContext:</b> Filter stores validated headers in ThreadLocal</li>
 *   <li><b>Access context:</b> RequestContextHolder.get() provides userId, correlationId</li>
 *   <li><b>MDC Logging:</b> Correlation ID automatically included in all log statements</li>
 * </ul>
 * 
 * <p><b>No Header Parameters:</b> This controller does NOT extract headers via @RequestHeader.
 * Headers are validated once by the filter and available via RequestContextHolder throughout
 * the request lifecycle. This follows DRY principle and single source of truth pattern.</p>
 * 
 * <p><b>Business Logic:</b> All business logic resides in the service layer.
 * This controller is a thin HTTP adapter with no domain logic.</p>
 * 
 * @see com.rbc.holidays.filter.RequestValidationFilter
 * @see com.rbc.holidays.context.RequestContextHolder
 * @see com.rbc.holidays.service.HolidayService
 */
@RestController
@RequestMapping("/api/v1/holidays")
@Tag(name = "Federal Holidays", description = "APIs for managing federal holidays for USA and Canada")
public class HolidayController {

    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);
    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    /**
     * Creates a new federal holiday.
     * Validates unique constraint: one holiday per country per date.
     * 
     * @param request Holiday details (name, date, country, isRecurring, description)
     * @return Created holiday with generated ID and timestamps
     */
    @Operation(summary = "Create a new federal holiday")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Holiday created successfully",
                    content = @Content(schema = @Schema(implementation = HolidayResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Holiday already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        RequestContext context = RequestContextHolder.get();
        logger.info("User '{}' creating holiday: {} on {} for {}", 
                   context.userId(), request.holidayName(), request.holidayDate(), request.country());
        
        HolidayResponse response = holidayService.createHoliday(request);
        
        logger.info("Holiday created successfully with id: {}", response.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing federal holiday.
     * All fields are replaced with new values from request.
     * 
     * @param id Holiday ID to update
     * @param request Updated holiday details
     * @return Updated holiday with new timestamp
     */
    @Operation(summary = "Update an existing federal holiday")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holiday updated successfully",
                    content = @Content(schema = @Schema(implementation = HolidayResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Holiday not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Holiday already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HolidayResponse> updateHoliday(
            @Parameter(description = "Holiday ID") @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request) {
        RequestContext context = RequestContextHolder.get();
        logger.info("User '{}' updating holiday id: {} to '{}' on {}", 
                   context.userId(), id, request.holidayName(), request.holidayDate());
        
        HolidayResponse response = holidayService.updateHoliday(id, request);
        
        logger.info("Holiday id: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single holiday by ID.
     * 
     * @param id Holiday ID
     * @return Holiday details
     */
    @Operation(summary = "Get a federal holiday by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holiday retrieved successfully",
                    content = @Content(schema = @Schema(implementation = HolidayResponse.class))),
            @ApiResponse(responseCode = "404", description = "Holiday not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HolidayResponse> getHolidayById(
            @Parameter(description = "Holiday ID") @PathVariable Long id) {
        logger.info("Fetching holiday with id: {}", id);
        HolidayResponse response = holidayService.getHolidayById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all federal holidays (USA and Canada).
     * No pagination - returns complete list.
     * 
     * @return List of all holidays
     */
    @Operation(summary = "Get all federal holidays")
    @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully")
    @GetMapping
    public ResponseEntity<List<HolidayResponse>> getAllHolidays() {
        logger.info("Fetching all holidays");
        List<HolidayResponse> response = holidayService.getAllHolidays();
        logger.debug("Retrieved {} holidays", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all holidays for a specific country.
     * 
     * @param country Country enum (USA or CANADA)
     * @return List of holidays for the specified country
     */
    @Operation(summary = "Get federal holidays by country")
    @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully")
    @GetMapping("/country/{country}")
    public ResponseEntity<List<HolidayResponse>> getHolidaysByCountry(
            @Parameter(description = "Country code (USA or CANADA)") @PathVariable Country country) {
        logger.info("Fetching holidays for country: {}", country);
        List<HolidayResponse> response = holidayService.getHolidaysByCountry(country);
        logger.debug("Retrieved {} holidays for {}", response.size(), country);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches holidays with flexible criteria.
     * Supports two modes:
     * 1. Date range: startDate + endDate (returns holidays in range)
     * 2. Country/Year: country and/or year (returns filtered holidays)
     * 
     * @param country Optional country filter (USA or CANADA)
     * @param year Optional year filter (YYYY format)
     * @param startDate Optional start date for range search (ISO 8601: YYYY-MM-DD)
     * @param endDate Optional end date for range search (ISO 8601: YYYY-MM-DD)
     * @return List of matching holidays
     */
    @Operation(summary = "Search holidays by country, year, or date range")
    @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully")
    @GetMapping("/search")
    public ResponseEntity<List<HolidayResponse>> searchHolidays(
            @Parameter(description = "Country code (USA or CANADA)") 
            @RequestParam(required = false) Country country,
            @Parameter(description = "Year (YYYY format)") 
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Start date (ISO 8601 format: YYYY-MM-DD)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601 format: YYYY-MM-DD)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.info("Searching holidays - country: {}, year: {}, startDate: {}, endDate: {}", 
                country, year, startDate, endDate);

        List<HolidayResponse> response;
        if (startDate != null && endDate != null) {
            response = holidayService.getHolidaysByDateRange(startDate, endDate);
            logger.debug("Found {} holidays between {} and {}", response.size(), startDate, endDate);
        } else {
            response = holidayService.getHolidaysByCountryAndYear(country, year);
            logger.debug("Found {} holidays for country: {}, year: {}", response.size(), country, year);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a holiday by ID.
     * Permanent deletion - cannot be undone.
     * 
     * @param id Holiday ID to delete
     * @return 204 No Content on success
     */
    @Operation(summary = "Delete a federal holiday")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Holiday deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Holiday not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(
            @Parameter(description = "Holiday ID") @PathVariable Long id) {
        RequestContext context = RequestContextHolder.get();
        logger.info("User '{}' deleting holiday id: {}", context.userId(), id);
        
        holidayService.deleteHoliday(id);
        
        logger.info("Holiday id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk upload holidays from CSV or JSON file.
     * Processes all records, returning success/failure counts.
     * Partial success allowed - some records may fail validation.
     * 
     * @param file Multipart file (CSV or JSON format, max 10MB)
     * @return Upload statistics (total, success, failure counts, error messages)
     */
    @Operation(summary = "Upload holidays from CSV or JSON file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadHolidays(
            @Parameter(description = "CSV or JSON file containing holidays") 
            @RequestParam("file") MultipartFile file) {
        RequestContext context = RequestContextHolder.get();
        logger.info("User '{}' uploading file: {} (size: {} bytes)", 
                   context.userId(), file.getOriginalFilename(), file.getSize());
        
        FileUploadResponse response = holidayService.uploadHolidaysFromFile(file);
        
        logger.info("File upload completed - Total: {}, Success: {}, Failed: {}", 
                   response.totalRecords(), response.successCount(), response.failureCount());
        return ResponseEntity.ok(response);
    }
}
