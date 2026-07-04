package com.rbc.holidays.service.impl;

import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.entity.FederalHoliday;
import com.rbc.holidays.exception.DuplicateHolidayException;
import com.rbc.holidays.exception.InvalidCountryException;
import com.rbc.holidays.exception.HolidayNotFoundException;
import com.rbc.holidays.exception.InvalidFileFormatException;
import com.rbc.holidays.repository.HolidayRepository;
import com.rbc.holidays.service.HolidayService;
import com.rbc.holidays.util.FileParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private static final Logger logger = LoggerFactory.getLogger(HolidayServiceImpl.class);
    private static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;
    private static final int MAX_UPLOAD_RECORDS = 1000;

    private final HolidayRepository holidayRepository;
    private final Set<String> supportedCountries;

    public HolidayServiceImpl(
            HolidayRepository holidayRepository,
            @Value("${holidays.supported-countries}") String supportedCountriesProperty) {
        this.holidayRepository = holidayRepository;
        this.supportedCountries = Arrays.stream(supportedCountriesProperty.split(","))
                .map(String::trim)
                .map(country -> country.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public HolidayResponse createHoliday(HolidayRequest request) {
        String country = normalizeAndValidateCountry(request.country());
        logger.info("Creating holiday: {} for country: {}", request.holidayName(), country);

        if (holidayRepository.existsByCountryAndHolidayDate(country, request.holidayDate())) {
            throw new DuplicateHolidayException(
                    String.format("Holiday already exists for %s on %s", 
                            country, request.holidayDate()));
        }

        FederalHoliday holiday = mapToEntity(request, country);
        FederalHoliday savedHoliday = holidayRepository.save(holiday);
        
        logger.info("Holiday created successfully with id: {}", savedHoliday.getId());
        return mapToResponse(savedHoliday);
    }

    @Override
    public HolidayResponse updateHoliday(Long id, HolidayRequest request) {
        String country = normalizeAndValidateCountry(request.country());
        logger.info("Updating holiday with id: {}", id);

        FederalHoliday existingHoliday = holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException(id));

        if (holidayRepository.existsByCountryAndHolidayDateAndIdNot(country, request.holidayDate(), id)) {
            throw new DuplicateHolidayException(
                String.format("Holiday already exists for %s on %s", 
                    country, request.holidayDate()));
        }

        existingHoliday.setHolidayName(request.holidayName());
        existingHoliday.setHolidayDate(request.holidayDate());
        existingHoliday.setCountry(country);
        existingHoliday.setIsRecurring(request.isRecurring() != null ? request.isRecurring() : true);
        existingHoliday.setDescription(request.description());

        FederalHoliday updatedHoliday = holidayRepository.save(existingHoliday);
        
        logger.info("Holiday updated successfully with id: {}", updatedHoliday.getId());
        return mapToResponse(updatedHoliday);
    }

    @Override
    @Transactional(readOnly = true)
    public HolidayResponse getHolidayById(Long id) {
        logger.info("Fetching holiday with id: {}", id);

        FederalHoliday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException(id));

        return mapToResponse(holiday);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getAllHolidays() {
        logger.info("Fetching all holidays");

        return holidayRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByCountry(String country) {
        String normalizedCountry = normalizeAndValidateCountry(country);
        logger.info("Fetching holidays for country: {}", normalizedCountry);

        return holidayRepository.findByCountry(normalizedCountry).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        logger.info("Fetching holidays between {} and {}", startDate, endDate);

        return holidayRepository.findByHolidayDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByCountryAndYear(String country, Integer year) {
        String normalizedCountry = country == null ? null : normalizeAndValidateCountry(country);
        if (year != null) {
            validateYear(year);
        }
        logger.info("Fetching holidays for country: {} and year: {}", normalizedCountry, year);

        if (normalizedCountry != null && year != null) {
            return holidayRepository.findByCountryAndYear(normalizedCountry, year).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (normalizedCountry != null) {
            return getHolidaysByCountry(normalizedCountry);
        } else if (year != null) {
            return holidayRepository.findByYear(year).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return getAllHolidays();
    }

    @Override
    public void deleteHoliday(Long id) {
        logger.info("Deleting holiday with id: {}", id);

        if (!holidayRepository.existsById(id)) {
            throw new HolidayNotFoundException(id);
        }

        holidayRepository.deleteById(id);
        logger.info("Holiday deleted successfully with id: {}", id);
    }

    @Override
    public FileUploadResponse uploadHolidaysFromFile(MultipartFile file) {
        logger.info("Processing file upload: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new InvalidFileFormatException("File is empty");
        }

        List<HolidayRequest> holidayRequests = parseHolidayRequestsFromFile(file);

        if (holidayRequests.isEmpty()) {
            throw new InvalidFileFormatException("File contains no valid holiday records");
        }

        if (holidayRequests.size() > MAX_UPLOAD_RECORDS) {
            throw new InvalidFileFormatException(
                    String.format("File contains %d records. Maximum allowed is %d", 
                            holidayRequests.size(), MAX_UPLOAD_RECORDS));
        }

        int totalRecords = holidayRequests.size();
        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < holidayRequests.size(); i++) {
            if (processUploadRow(i + 1, holidayRequests.get(i), errors)) {
                successCount++;
            }
        }

        int failureCount = totalRecords - successCount;
        String message = String.format("File processed successfully. %d out of %d records imported.",
                successCount, totalRecords);

        logger.info("File upload completed. Success: {}, Failures: {}", successCount, failureCount);

        return new FileUploadResponse(
                totalRecords,
                successCount,
                failureCount,
                errors,
                message
        );
    }

    /**
     * Parses holiday records from uploaded file based on content type.
     * Supports CSV (text/csv) and JSON (application/json) formats.
     * 
     * @param file Uploaded multipart file
     * @return List of parsed holiday requests
     * @throws InvalidFileFormatException if file format is unsupported or parsing fails
     */
    private List<HolidayRequest> parseHolidayRequestsFromFile(MultipartFile file) {
        String contentType = file.getContentType();
        try {
            if (CONTENT_TYPE_CSV.equals(contentType)) {
                return FileParserUtil.parseCsvFile(file);
            }
            if (CONTENT_TYPE_JSON.equals(contentType)) {
                return FileParserUtil.parseJsonFile(file);
            }
            throw new InvalidFileFormatException(
                    "Unsupported file format. Only CSV and JSON files are allowed.");
        } catch (Exception e) {
            throw new InvalidFileFormatException("Error parsing file: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a single row from file upload.
     * Validates country, checks for duplicates, and saves if valid.
     * 
     * @param rowNumber Row number for error reporting (1-based)
     * @param request Holiday request from file
     * @param errors List to accumulate error messages
     * @return true if row processed successfully, false otherwise
     */
    private boolean processUploadRow(int rowNumber, HolidayRequest request, List<String> errors) {
        try {
            String country = normalizeAndValidateCountry(request.country());
            if (holidayRepository.existsByCountryAndHolidayDate(country, request.holidayDate())) {
                errors.add(String.format("Row %d: Holiday already exists for %s on %s",
                        rowNumber, country, request.holidayDate()));
                return false;
            }

            holidayRepository.save(mapToEntity(request, country));
            return true;
        } catch (Exception e) {
            errors.add(String.format("Row %d: %s", rowNumber, e.getMessage()));
            return false;
        }
    }

    /**
     * Normalizes and validates a country code.
     * Trims whitespace, converts to uppercase, and checks against supported countries.
     * 
     * @param country Country code to validate
     * @return Normalized country code (uppercase, trimmed)
     * @throws InvalidCountryException if country is null, blank, or not supported
     */
    private String normalizeAndValidateCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new InvalidCountryException(String.valueOf(country), String.join(", ", getSupportedCountriesForMessage()));
        }

        String normalized = country.trim().toUpperCase(Locale.ROOT);
        if (!supportedCountries.contains(normalized)) {
            throw new InvalidCountryException(normalized, String.join(", ", getSupportedCountriesForMessage()));
        }

        return normalized;
    }

    /**
     * Gets a sorted list of supported countries for error messages.
     * 
     * @return Sorted list of supported country codes
     */
    private List<String> getSupportedCountriesForMessage() {
        List<String> sortedCountries = new ArrayList<>(supportedCountries);
        Collections.sort(sortedCountries);
        return sortedCountries;
    }

    /**
     * Validates that start and end dates are both provided and startDate <= endDate.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @throws IllegalArgumentException if validation fails
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both startDate and endDate must be provided for date range search");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    String.format("Start date %s cannot be after end date %s", startDate, endDate));
        }
    }

    /**
     * Validates that the year is within acceptable bounds.
     * 
     * @param year Year to validate
     * @throws IllegalArgumentException if year is out of bounds
     */
    private void validateYear(Integer year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    String.format("Year must be between %d and %d", MIN_YEAR, MAX_YEAR));
        }
    }

    /**
     * Maps a holiday request DTO to a FederalHoliday entity.
     * 
     * @param request Holiday request DTO
     * @param normalizedCountry Already validated and normalized country code
     * @return FederalHoliday entity ready for persistence
     */
    private FederalHoliday mapToEntity(HolidayRequest request, String normalizedCountry) {
        return FederalHoliday.builder()
                .holidayName(request.holidayName())
                .holidayDate(request.holidayDate())
                .country(normalizedCountry)
                .isRecurring(request.isRecurring() != null ? request.isRecurring() : true)
                .description(request.description())
                .build();
    }

    /**
     * Maps a FederalHoliday entity to a response DTO.
     * 
     * @param holiday FederalHoliday entity
     * @return HolidayResponse DTO for API response
     */
    private HolidayResponse mapToResponse(FederalHoliday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getHolidayName(),
                holiday.getHolidayDate(),
                holiday.getCountry(),
                holiday.getIsRecurring(),
                holiday.getDescription()
        );
    }
}
