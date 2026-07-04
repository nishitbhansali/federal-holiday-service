package com.rbc.holidays.controller;

import com.rbc.holidays.context.RequestContext;
import com.rbc.holidays.context.RequestContextHolder;
import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.service.HolidayService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayControllerTest {

    @Mock
    private HolidayService holidayService;

    private HolidayController controller;

    @BeforeEach
    void setUp() {
        controller = new HolidayController(holidayService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void createHolidayShouldReturnCreatedResponse() {
        HolidayRequest request = new HolidayRequest("Independence Day", LocalDate.of(2026, 7, 4), "USA", true, "Federal holiday");
        HolidayResponse response = new HolidayResponse(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true, "Federal holiday");
        RequestContextHolder.set(new RequestContext("123e4567-e89b-12d3-a456-426614174000", "tester", "/api/v1/holidays"));
        when(holidayService.createHoliday(request)).thenReturn(response);

        var entity = controller.createHoliday(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).createHoliday(request);
    }

    @Test
    void updateHolidayShouldReturnOkResponse() {
        HolidayRequest request = new HolidayRequest("Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true, "National holiday");
        HolidayResponse response = new HolidayResponse(2L, "Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true, "National holiday");
        RequestContextHolder.set(new RequestContext("123e4567-e89b-12d3-a456-426614174000", "tester", "/api/v1/holidays/2"));
        when(holidayService.updateHoliday(2L, request)).thenReturn(response);

        var entity = controller.updateHoliday(2L, request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).updateHoliday(2L, request);
    }

    @Test
    void getHolidayByIdShouldReturnOkResponse() {
        HolidayResponse response = new HolidayResponse(3L, "Christmas Day", LocalDate.of(2026, 12, 25), "USA", true, "Holiday");
        when(holidayService.getHolidayById(3L)).thenReturn(response);

        var entity = controller.getHolidayById(3L);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void getAllHolidaysShouldReturnOkResponse() {
        List<HolidayResponse> response = List.of(
                new HolidayResponse(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true, "Holiday")
        );
        when(holidayService.getAllHolidays()).thenReturn(response);

        var entity = controller.getAllHolidays();

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void getHolidaysByCountryShouldReturnOkResponse() {
        List<HolidayResponse> response = List.of(
                new HolidayResponse(2L, "Canada Day", LocalDate.of(2026, 7, 1), "CANADA", true, "Holiday")
        );
        when(holidayService.getHolidaysByCountry("CANADA")).thenReturn(response);

        var entity = controller.getHolidaysByCountry("CANADA");

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void searchHolidaysShouldUseDateRangeWhenDatesProvided() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        List<HolidayResponse> response = List.of(
                new HolidayResponse(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true, "Holiday")
        );
        when(holidayService.getHolidaysByDateRange(startDate, endDate)).thenReturn(response);

        var entity = controller.searchHolidays(null, null, startDate, endDate);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).getHolidaysByDateRange(startDate, endDate);
    }

    @Test
    void searchHolidaysShouldUseCountryAndYearWhenDateRangeMissing() {
        List<HolidayResponse> response = List.of(
                new HolidayResponse(1L, "Boxing Day", LocalDate.of(2026, 12, 26), "CANADA", true, "Holiday")
        );
        when(holidayService.getHolidaysByCountryAndYear("CANADA", 2026)).thenReturn(response);

        var entity = controller.searchHolidays("CANADA", 2026, null, null);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).getHolidaysByCountryAndYear("CANADA", 2026);
    }

    @Test
    void searchHolidaysShouldFallbackToCountryAndYearWhenOnlyOneDateIsProvided() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        List<HolidayResponse> response = List.of(
                new HolidayResponse(1L, "Independence Day", LocalDate.of(2026, 7, 4), "USA", true, "Holiday")
        );
        when(holidayService.getHolidaysByCountryAndYear("USA", 2026)).thenReturn(response);

        var entity = controller.searchHolidays("USA", 2026, startDate, null);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).getHolidaysByCountryAndYear("USA", 2026);
    }

    @Test
    void deleteHolidayShouldReturnNoContent() {
        RequestContextHolder.set(new RequestContext("123e4567-e89b-12d3-a456-426614174000", "tester", "/api/v1/holidays/4"));

        var entity = controller.deleteHoliday(4L);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(holidayService).deleteHoliday(4L);
    }

    @Test
    void uploadHolidaysShouldReturnOkResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", "header".getBytes());
        FileUploadResponse response = new FileUploadResponse(1, 1, 0, List.of(), "Processed successfully");
        RequestContextHolder.set(new RequestContext("123e4567-e89b-12d3-a456-426614174000", "tester", "/api/v1/holidays/upload"));
        when(holidayService.uploadHolidaysFromFile(file)).thenReturn(response);

        var entity = controller.uploadHolidays(file);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(holidayService).uploadHolidaysFromFile(file);
    }
}