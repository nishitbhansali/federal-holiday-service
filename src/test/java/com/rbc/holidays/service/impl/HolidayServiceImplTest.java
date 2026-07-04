package com.rbc.holidays.service.impl;

import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.entity.FederalHoliday;
import com.rbc.holidays.exception.DuplicateHolidayException;
import com.rbc.holidays.exception.HolidayNotFoundException;
import com.rbc.holidays.exception.InvalidCountryException;
import com.rbc.holidays.exception.InvalidFileFormatException;
import com.rbc.holidays.repository.HolidayRepository;
import com.rbc.holidays.util.FileParserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

    @Mock
    private HolidayRepository holidayRepository;

    private HolidayServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HolidayServiceImpl(holidayRepository, "USA,CANADA");
    }

    @Test
    void createHolidayShouldNormalizeCountryAndDefaultRecurringFlag() {
        HolidayRequest request = new HolidayRequest("Independence Day", LocalDate.of(2026, 7, 4), " usa ", null, "Federal holiday");
        when(holidayRepository.existsByCountryAndHolidayDate("USA", request.holidayDate())).thenReturn(false);
        when(holidayRepository.save(any(FederalHoliday.class))).thenAnswer(invocation -> {
            FederalHoliday holiday = invocation.getArgument(0);
            holiday.setId(1L);
            return holiday;
        });

        HolidayResponse response = service.createHoliday(request);

        ArgumentCaptor<FederalHoliday> captor = ArgumentCaptor.forClass(FederalHoliday.class);
        verify(holidayRepository).save(captor.capture());
        assertThat(captor.getValue().getCountry()).isEqualTo("USA");
        assertThat(captor.getValue().getIsRecurring()).isTrue();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.country()).isEqualTo("USA");
        assertThat(response.isRecurring()).isTrue();
    }

    @Test
    void createHolidayShouldThrowWhenDuplicateExists() {
        HolidayRequest request = request("Independence Day", LocalDate.of(2026, 7, 4), "USA", true);
        when(holidayRepository.existsByCountryAndHolidayDate("USA", request.holidayDate())).thenReturn(true);

        assertThatThrownBy(() -> service.createHoliday(request))
                .isInstanceOf(DuplicateHolidayException.class)
                .hasMessage("Holiday already exists for USA on 2026-07-04");
    }

    @Test
    void updateHolidayShouldThrowWhenHolidayMissing() {
        HolidayRequest request = request("Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true);
        when(holidayRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateHoliday(9L, request))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessage("Holiday not found with id: 9");
    }

    @Test
    void updateHolidayShouldThrowWhenAnotherHolidayConflicts() {
        HolidayRequest request = request("Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true);
        when(holidayRepository.findById(2L)).thenReturn(Optional.of(holidayEntity(2L, "Old", LocalDate.of(2025, 7, 1), "CANADA", false)));
        when(holidayRepository.existsByCountryAndHolidayDateAndIdNot("CANADA", request.holidayDate(), 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateHoliday(2L, request))
                .isInstanceOf(DuplicateHolidayException.class)
                .hasMessage("Holiday already exists for CANADA on 2026-07-01");
    }

    @Test
    void updateHolidayShouldUpdateFieldsAndDefaultRecurringFlag() {
        FederalHoliday existing = holidayEntity(2L, "Old Name", LocalDate.of(2025, 1, 1), "USA", false);
        HolidayRequest request = new HolidayRequest("Canada Day", LocalDate.of(2026, 7, 1), " canada ", null, "Updated");
        when(holidayRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(holidayRepository.existsByCountryAndHolidayDateAndIdNot("CANADA", request.holidayDate(), 2L)).thenReturn(false);
        when(holidayRepository.save(existing)).thenReturn(existing);

        HolidayResponse response = service.updateHoliday(2L, request);

        assertThat(existing.getHolidayName()).isEqualTo("Canada Day");
        assertThat(existing.getHolidayDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(existing.getCountry()).isEqualTo("CANADA");
        assertThat(existing.getIsRecurring()).isTrue();
        assertThat(existing.getDescription()).isEqualTo("Updated");
        assertThat(response.country()).isEqualTo("CANADA");
    }

    @Test
    void getHolidayByIdShouldMapEntity() {
        when(holidayRepository.findById(5L)).thenReturn(Optional.of(holidayEntity(5L, "Christmas", LocalDate.of(2026, 12, 25), "USA", true)));

        HolidayResponse response = service.getHolidayById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.holidayName()).isEqualTo("Christmas");
    }

    @Test
    void getHolidayByIdShouldThrowWhenMissing() {
        when(holidayRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHolidayById(5L))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessage("Holiday not found with id: 5");
    }

    @Test
    void getAllHolidaysShouldReturnMappedResponses() {
        when(holidayRepository.findAll()).thenReturn(List.of(
                holidayEntity(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true),
                holidayEntity(2L, "Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true)
        ));

        List<HolidayResponse> responses = service.getAllHolidays();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(HolidayResponse::country).containsExactly("USA", "CANADA");
    }

    @Test
    void getHolidaysByCountryShouldNormalizeCountryAndReturnMatches() {
        when(holidayRepository.findByCountry("USA")).thenReturn(List.of(
                holidayEntity(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true)
        ));

        List<HolidayResponse> responses = service.getHolidaysByCountry(" usa ");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().country()).isEqualTo("USA");
    }

    @Test
    void getHolidaysByCountryShouldThrowForUnsupportedCountry() {
        assertThatThrownBy(() -> service.getHolidaysByCountry("MARS"))
                .isInstanceOf(InvalidCountryException.class)
                .hasMessage("Invalid country 'MARS'. Supported countries: CANADA, USA");
    }

    @Test
    void getHolidaysByCountryShouldThrowForBlankCountry() {
        assertThatThrownBy(() -> service.getHolidaysByCountry(" "))
                .isInstanceOf(InvalidCountryException.class)
                .hasMessage("Invalid country ' '. Supported countries: CANADA, USA");
    }

    @Test
    void createHolidayShouldPreserveExplicitRecurringFalse() {
        HolidayRequest request = new HolidayRequest("One Time Holiday", LocalDate.of(2026, 11, 1), "USA", false, "One-time");
        when(holidayRepository.existsByCountryAndHolidayDate("USA", request.holidayDate())).thenReturn(false);
        when(holidayRepository.save(any(FederalHoliday.class))).thenAnswer(invocation -> {
            FederalHoliday holiday = invocation.getArgument(0);
            holiday.setId(11L);
            return holiday;
        });

        HolidayResponse response = service.createHoliday(request);

        assertThat(response.isRecurring()).isFalse();
    }

    @Test
    void getHolidaysByDateRangeShouldValidateDates() {
        assertThatThrownBy(() -> service.getHolidaysByDateRange(null, LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both startDate and endDate must be provided for date range search");

        assertThatThrownBy(() -> service.getHolidaysByDateRange(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date 2026-12-31 cannot be after end date 2026-01-01");
    }

    @Test
    void getHolidaysByDateRangeShouldReturnMatches() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        when(holidayRepository.findByHolidayDateBetween(startDate, endDate)).thenReturn(List.of(
                holidayEntity(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true)
        ));

        List<HolidayResponse> responses = service.getHolidaysByDateRange(startDate, endDate);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().holidayName()).isEqualTo("Independence Day");
    }

    @Test
    void getHolidaysByCountryAndYearShouldHandleAllSearchModes() {
        when(holidayRepository.findByCountryAndYear("USA", 2026)).thenReturn(List.of(
                holidayEntity(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true)
        ));
        when(holidayRepository.findByCountry("CANADA")).thenReturn(List.of(
                holidayEntity(2L, "Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true)
        ));
        when(holidayRepository.findByYear(2027)).thenReturn(List.of(
                holidayEntity(3L, "Boxing Day", LocalDate.of(2027, 12, 26), "CANADA", true)
        ));
        when(holidayRepository.findAll()).thenReturn(List.of(
                holidayEntity(4L, "Christmas", LocalDate.of(2026, 12, 25), "USA", true)
        ));

        assertThat(service.getHolidaysByCountryAndYear("USA", 2026)).hasSize(1);
        assertThat(service.getHolidaysByCountryAndYear("CANADA", null)).hasSize(1);
        assertThat(service.getHolidaysByCountryAndYear(null, 2027)).hasSize(1);
        assertThat(service.getHolidaysByCountryAndYear(null, null)).hasSize(1);
    }

    @Test
    void deleteHolidayShouldDeleteExistingHoliday() {
        when(holidayRepository.existsById(10L)).thenReturn(true);

        service.deleteHoliday(10L);

        verify(holidayRepository).deleteById(10L);
    }

    @Test
    void deleteHolidayShouldThrowWhenMissing() {
        when(holidayRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteHoliday(10L))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessage("Holiday not found with id: 10");
    }

    @Test
    void uploadHolidaysFromFileShouldRejectEmptyMultipartFile() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> service.uploadHolidaysFromFile(file))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessage("File is empty");
    }

    @Test
    void uploadHolidaysFromFileShouldRejectEmptyParsedRecords() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "content".getBytes());

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseCsvFile(file)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.uploadHolidaysFromFile(file))
                    .isInstanceOf(InvalidFileFormatException.class)
                    .hasMessage("File contains no valid holiday records");
        }
    }

    @Test
    void uploadHolidaysFromFileShouldRejectMoreThanMaximumRecords() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "content".getBytes());
        List<HolidayRequest> requests = java.util.stream.IntStream.range(0, 1001)
                .mapToObj(i -> request("Holiday" + i, LocalDate.of(2026, 1, 1).plusDays(i % 365), "USA", true))
                .toList();

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseCsvFile(file)).thenReturn(requests);

            assertThatThrownBy(() -> service.uploadHolidaysFromFile(file))
                    .isInstanceOf(InvalidFileFormatException.class)
                    .hasMessage("File contains 1001 records. Maximum allowed is 1000");
        }
    }

    @Test
    void uploadHolidaysFromFileShouldWrapUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.xml", "application/xml", "content".getBytes());

        assertThatThrownBy(() -> service.uploadHolidaysFromFile(file))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("Unsupported file format. Only CSV and JSON files are allowed.");
    }

    @Test
    void uploadHolidaysFromFileShouldWrapParserFailures() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "content".getBytes());

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseCsvFile(file)).thenThrow(new RuntimeException("parse failed"));

            assertThatThrownBy(() -> service.uploadHolidaysFromFile(file))
                    .isInstanceOf(InvalidFileFormatException.class)
                    .hasMessageContaining("Error parsing file: parse failed");
        }
    }

    @Test
    void uploadHolidaysFromFileShouldSupportJsonAndReturnSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.json", "application/json", "[]".getBytes());
        HolidayRequest request = request("Christmas Day", LocalDate.of(2026, 12, 25), "USA", true);

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseJsonFile(file)).thenReturn(List.of(request));
            when(holidayRepository.existsByCountryAndHolidayDate("USA", request.holidayDate())).thenReturn(false);
            when(holidayRepository.save(any(FederalHoliday.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FileUploadResponse response = service.uploadHolidaysFromFile(file);

            assertThat(response.totalRecords()).isEqualTo(1);
            assertThat(response.successCount()).isEqualTo(1);
            assertThat(response.failureCount()).isEqualTo(0);
            assertThat(response.errors()).isEmpty();
        }
    }

    @Test
    void uploadHolidaysFromFileShouldSupportCsvAndReturnSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "header".getBytes());
        HolidayRequest request = request("Labour Day", LocalDate.of(2026, 9, 7), "CANADA", false);

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseCsvFile(file)).thenReturn(List.of(request));
            when(holidayRepository.existsByCountryAndHolidayDate("CANADA", request.holidayDate())).thenReturn(false);
            when(holidayRepository.save(any(FederalHoliday.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FileUploadResponse response = service.uploadHolidaysFromFile(file);

            assertThat(response.totalRecords()).isEqualTo(1);
            assertThat(response.successCount()).isEqualTo(1);
            assertThat(response.failureCount()).isEqualTo(0);
            assertThat(response.errors()).isEmpty();
        }
    }

    @Test
    void uploadHolidaysFromFileShouldReturnPartialSuccessWithRowErrors() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "content".getBytes());
        HolidayRequest valid = request("Independence Day", LocalDate.of(2026, 7, 4), "USA", true);
        HolidayRequest duplicate = request("Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true);
        HolidayRequest invalidCountry = request("Mars Day", LocalDate.of(2026, 3, 1), "MARS", true);

        try (MockedStatic<FileParserUtil> parser = mockStatic(FileParserUtil.class)) {
            parser.when(() -> FileParserUtil.parseCsvFile(file)).thenReturn(List.of(valid, duplicate, invalidCountry));
            when(holidayRepository.existsByCountryAndHolidayDate("USA", valid.holidayDate())).thenReturn(false);
            when(holidayRepository.existsByCountryAndHolidayDate("CANADA", duplicate.holidayDate())).thenReturn(true);
            when(holidayRepository.save(any(FederalHoliday.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FileUploadResponse response = service.uploadHolidaysFromFile(file);

            assertThat(response.totalRecords()).isEqualTo(3);
            assertThat(response.successCount()).isEqualTo(1);
            assertThat(response.failureCount()).isEqualTo(2);
            assertThat(response.errors()).hasSize(2);
            assertThat(response.errors().getFirst()).isEqualTo("Row 2: Holiday already exists for CANADA on 2026-07-01");
            assertThat(response.errors().get(1)).isEqualTo("Row 3: Invalid country 'MARS'. Supported countries: CANADA, USA");
            verify(holidayRepository, never()).existsByCountryAndHolidayDate(eq("MARS"), any(LocalDate.class));
        }
    }

    private HolidayRequest request(String holidayName, LocalDate holidayDate, String country, Boolean isRecurring) {
        return new HolidayRequest(holidayName, holidayDate, country, isRecurring, "Description");
    }

    private FederalHoliday holidayEntity(Long id, String holidayName, LocalDate holidayDate, String country, Boolean recurring) {
        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(id);
        holiday.setHolidayName(holidayName);
        holiday.setHolidayDate(holidayDate);
        holiday.setCountry(country);
        holiday.setIsRecurring(recurring);
        holiday.setDescription("Description");
        return holiday;
    }
}