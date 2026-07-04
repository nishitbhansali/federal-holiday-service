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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private static final Logger logger = LoggerFactory.getLogger(HolidayServiceImpl.class);
    private final HolidayRepository holidayRepository;
    
    @Value("#{'${holidays.supported-countries}'.split(',')}")
    private List<String> supportedCountries;

    public HolidayServiceImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }
    
    /**
     * Validates that the country is in the supported countries list.
     * 
     * @param country Country code to validate
     * @throws InvalidCountryException if country is not supported
     */
    private void validateCountry(String country) {
        if (!supportedCountries.contains(country)) {
            throw new InvalidCountryException(country, String.join(", ", supportedCountries));
        }
    }

    @Override
    public HolidayResponse createHoliday(HolidayRequest request) {
        logger.info("Creating holiday: {} for country: {}", request.holidayName(), request.country());
        
        validateCountry(request.country());

        if (holidayRepository.existsByCountryAndHolidayDate(request.country(), request.holidayDate())) {
            throw new DuplicateHolidayException(
                    String.format("Holiday already exists for %s on %s", 
                            request.country(), request.holidayDate()));
        }

        FederalHoliday holiday = mapToEntity(request);
        FederalHoliday savedHoliday = holidayRepository.save(holiday);
        
        logger.info("Holiday created successfully with id: {}", savedHoliday.getId());
        return mapToResponse(savedHoliday);
    }

    @Override
    public HolidayResponse updateHoliday(Long id, HolidayRequest request) {
        logger.info("Updating holiday with id: {}", id);
        
        validateCountry(request.country());

        FederalHoliday existingHoliday = holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException(id));

        if (!existingHoliday.getCountry().equals(request.country()) || 
            !existingHoliday.getHolidayDate().equals(request.holidayDate())) {
            
            if (holidayRepository.existsByCountryAndHolidayDate(request.country(), request.holidayDate())) {
                throw new DuplicateHolidayException(
                        String.format("Holiday already exists for %s on %s", 
                                request.country(), request.holidayDate()));
            }
        }

        existingHoliday.setHolidayName(request.holidayName());
        existingHoliday.setHolidayDate(request.holidayDate());
        existingHoliday.setCountry(request.country());
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
        logger.info("Fetching holidays for country: {}", country);

        return holidayRepository.findByCountry(country).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching holidays between {} and {}", startDate, endDate);

        return holidayRepository.findByHolidayDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByCountryAndYear(String country, Integer year) {
        logger.info("Fetching holidays for country: {} and year: {}", country, year);

        if (country != null && year != null) {
            return holidayRepository.findByCountryAndYear(country, year).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (country != null) {
            return getHolidaysByCountry(country);
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

        String contentType = file.getContentType();
        List<HolidayRequest> holidayRequests;

        try {
            if ("text/csv".equals(contentType)) {
                holidayRequests = FileParserUtil.parseCsvFile(file);
            } else if ("application/json".equals(contentType)) {
                holidayRequests = FileParserUtil.parseJsonFile(file);
            } else {
                throw new InvalidFileFormatException(
                        "Unsupported file format. Only CSV and JSON files are allowed.");
            }
        } catch (Exception e) {
            throw new InvalidFileFormatException("Error parsing file: " + e.getMessage(), e);
        }

        int totalRecords = holidayRequests.size();
        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < holidayRequests.size(); i++) {
            HolidayRequest request = holidayRequests.get(i);
            try {
                if (!holidayRepository.existsByCountryAndHolidayDate(
                        request.country(), request.holidayDate())) {
                    FederalHoliday holiday = mapToEntity(request);
                    holidayRepository.save(holiday);
                    successCount++;
                } else {
                    errors.add(String.format("Row %d: Holiday already exists for %s on %s",
                            i + 1, request.country(), request.holidayDate()));
                }
            } catch (Exception e) {
                errors.add(String.format("Row %d: %s", i + 1, e.getMessage()));
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

    private FederalHoliday mapToEntity(HolidayRequest request) {
        return FederalHoliday.builder()
                .holidayName(request.holidayName())
                .holidayDate(request.holidayDate())
                .country(request.country())
                .isRecurring(request.isRecurring() != null ? request.isRecurring() : true)
                .description(request.description())
                .build();
    }

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
