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

public class FileParserUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static List<HolidayRequest> parseCsvFile(MultipartFile file) throws Exception {
        List<HolidayRequest> holidays = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                HolidayRequest request = HolidayRequest.builder()
                        .holidayName(csvRecord.get("holidayName"))
                        .holidayDate(LocalDate.parse(csvRecord.get("holidayDate"), DATE_FORMATTER))
                        .country(Country.valueOf(csvRecord.get("country").toUpperCase()))
                        .isRecurring(Boolean.parseBoolean(csvRecord.get("isRecurring")))
                        .description(csvRecord.get("description"))
                        .build();

                holidays.add(request);
            }
        }

        return holidays;
    }

    public static List<HolidayRequest> parseJsonFile(MultipartFile file) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<List<HolidayRequest>>() {});
    }
}
