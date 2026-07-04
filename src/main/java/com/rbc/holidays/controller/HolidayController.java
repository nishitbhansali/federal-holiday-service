package com.rbc.holidays.controller;

import com.rbc.holidays.context.RequestContext;
import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.service.HolidayService;
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
    @PutMapping("/{id}")
    public ResponseEntity<HolidayResponse> updateHoliday(
            @PathVariable Long id,
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
    @GetMapping("/{id}")
    public ResponseEntity<HolidayResponse> getHolidayById(@PathVariable Long id) {
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
     * @param country Country code (e.g., USA, CANADA, SPAIN)
     * @return List of holidays for the specified country
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<HolidayResponse>> getHolidaysByCountry(@PathVariable String country) {
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
     * @param country Optional country filter (e.g., USA, CANADA, SPAIN)
     * @param year Optional year filter (YYYY format)
     * @param startDate Optional start date for range search (ISO 8601: YYYY-MM-DD)
     * @param endDate Optional end date for range search (ISO 8601: YYYY-MM-DD)
     * @return List of matching holidays
     */
    @GetMapping("/search")
    public ResponseEntity<List<HolidayResponse>> searchHolidays(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
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
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadHolidays(@RequestParam("file") MultipartFile file) {
        RequestContext context = RequestContextHolder.get();
        logger.info("User '{}' uploading file: {} (size: {} bytes)", 
                   context.userId(), file.getOriginalFilename(), file.getSize());
        
        FileUploadResponse response = holidayService.uploadHolidaysFromFile(file);
        
        logger.info("File upload completed - Total: {}, Success: {}, Failed: {}", 
                   response.totalRecords(), response.successCount(), response.failureCount());
        return ResponseEntity.ok(response);
    }
}
