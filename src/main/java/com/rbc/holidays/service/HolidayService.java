package com.rbc.holidays.service;

import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing federal holidays.
 * 
 * <p>This service provides business logic for CRUD operations on federal holidays.
 * All methods are transactional and include validation logic.</p>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Create, update, read, delete federal holidays</li>
 *   <li>Query holidays by country, year, or date range</li>
 *   <li>Import holidays from CSV or JSON files</li>
 *   <li>Validate business rules (no duplicate holidays for same country/date)</li>
 *   <li>Map between DTOs and entities</li>
 * </ul>
 * 
 * <p><strong>Exception Handling:</strong></p>
 * <ul>
 *   <li>HolidayNotFoundException - When holiday ID doesn't exist</li>
 *   <li>DuplicateHolidayException - When holiday already exists for country/date</li>
 *   <li>InvalidFileFormatException - When uploaded file format is invalid</li>
 * </ul>
 */
public interface HolidayService {

    /**
     * Creates a new federal holiday.
     * Validates that holiday doesn't already exist for the country and date.
     * 
     * @param request Holiday details
     * @return Created holiday with generated ID and timestamps
     * @throws DuplicateHolidayException if holiday already exists
     */
    HolidayResponse createHoliday(HolidayRequest request);

    /**
     * Updates an existing federal holiday.
     * All fields are replaced with new values from request.
     * 
     * @param id Holiday ID to update
     * @param request Updated holiday details
     * @return Updated holiday with new updatedAt timestamp
     * @throws HolidayNotFoundException if holiday doesn't exist
     * @throws DuplicateHolidayException if updated date conflicts with existing holiday
     */
    HolidayResponse updateHoliday(Long id, HolidayRequest request);

    /**
     * Retrieves a holiday by its ID.
     * 
     * @param id Holiday ID
     * @return Holiday details
     * @throws HolidayNotFoundException if holiday doesn't exist
     */
    HolidayResponse getHolidayById(Long id);

    /**
     * Retrieves all federal holidays across all countries.
     * 
     * @return List of all holidays
     */
    List<HolidayResponse> getAllHolidays();

    /**
     * Retrieves all holidays for a specific country.
     * 
     * @param country Country code to filter by (e.g., USA, CANADA, SPAIN)
     * @return List of holidays for the country
     */
    List<HolidayResponse> getHolidaysByCountry(String country);

    /**
     * Retrieves holidays within a date range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of holidays in the date range
     */
    List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves holidays filtered by country and/or year.
     * Flexible query - either or both parameters can be provided.
     * 
     * @param country Country code to filter by (optional)
     * @param year Year to filter by (optional)
     * @return List of holidays matching criteria
     */
    List<HolidayResponse> getHolidaysByCountryAndYear(String country, Integer year);

    /**
     * Deletes a federal holiday by ID.
     * 
     * @param id Holiday ID to delete
     * @throws HolidayNotFoundException if holiday doesn't exist
     */
    void deleteHoliday(Long id);

    /**
     * Imports holidays from a CSV or JSON file.
     * Supports bulk upload with error handling for each record.
     * 
     * @param file Multipart file (CSV or JSON)
     * @return Upload response with success/failure counts and error details
     * @throws InvalidFileFormatException if file format is unsupported
     */
    FileUploadResponse uploadHolidaysFromFile(MultipartFile file);
}
