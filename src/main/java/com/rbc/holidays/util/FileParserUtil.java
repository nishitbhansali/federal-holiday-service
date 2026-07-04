package com.rbc.holidays.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.enums.Country;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing holiday data from uploaded files (CSV and JSON).
 * 
 * <p><strong>Supported File Formats:</strong></p>
 * <ul>
 *   <li>CSV - text/csv with headers: holidayName, holidayDate, country, isRecurring, description</li>
 *   <li>JSON - application/json with array of holiday objects matching HolidayRequest structure</li>
 * </ul>
 * 
 * <p><strong>CSV Format Requirements:</strong></p>
 * <ul>
 *   <li>First row must contain header names (case-insensitive)</li>
 *   <li>holidayDate must be in ISO 8601 format (YYYY-MM-DD)</li>
 *   <li>country must be USA or CANADA</li>
 *   <li>isRecurring must be true or false</li>
 * </ul>
 * 
 * <p><strong>Example CSV:</strong></p>
 * <pre>
 * holidayName,holidayDate,country,isRecurring,description
 * Independence Day,2026-07-04,USA,true,Celebrates US independence
 * Canada Day,2026-07-01,CANADA,true,National day of Canada
 * </pre>
 * 
 * <p><strong>Example JSON:</strong></p>
 * <pre>
 * [
 *   {
 *     "holidayName": "Independence Day",
 *     "holidayDate": "2026-07-04",
 *     "country": "USA",
 *     "isRecurring": true,
 *     "description": "Celebrates US independence"
 *   }
 * ]
 * </pre>
 * 
 * @see HolidayRequest
 * @see com.rbc.holidays.service.HolidayServiceImpl#uploadHolidaysFromFile(MultipartFile)
 */
public class FileParserUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FileParserUtil() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Parses holidays from a CSV file.
     * 
     * <p>Expected CSV format:</p>
     * <ul>
     *   <li>Header row with column names (case-insensitive)</li>
     *   <li>Required columns: holidayName, holidayDate, country, isRecurring, description</li>
     *   <li>Date format: ISO 8601 (YYYY-MM-DD)</li>
     * </ul>
     * 
     * @param file CSV file uploaded via multipart/form-data
     * @return List of HolidayRequest objects parsed from CSV
     * @throws Exception if file cannot be read or CSV format is invalid
     */
    public static List<HolidayRequest> parseCsvFile(MultipartFile file) throws Exception {
        List<HolidayRequest> holidays = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord csvRecord : csvParser) {
                HolidayRequest request = new HolidayRequest(
                        csvRecord.get("holidayName"),
                        LocalDate.parse(csvRecord.get("holidayDate"), DATE_FORMATTER),
                        Country.valueOf(csvRecord.get("country").toUpperCase()),
                        Boolean.parseBoolean(csvRecord.get("isRecurring")),
                        csvRecord.get("description")
                );

                holidays.add(request);
            }
        }

        return holidays;
    }

    /**
     * Parses holidays from a JSON file.
     * 
     * <p>Expected JSON structure:</p>
     * <pre>
     * [
     *   {
     *     "holidayName": "string",
     *     "holidayDate": "YYYY-MM-DD",
     *     "country": "USA|CANADA",
     *     "isRecurring": boolean,
     *     "description": "string"
     *   }
     * ]
     * </pre>
     * 
     * @param file JSON file uploaded via multipart/form-data
     * @return List of HolidayRequest objects parsed from JSON
     * @throws Exception if file cannot be read or JSON format is invalid
     */
    public static List<HolidayRequest> parseJsonFile(MultipartFile file) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<List<HolidayRequest>>() {});
    }
}
