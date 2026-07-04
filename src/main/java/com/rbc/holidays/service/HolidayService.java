package com.rbc.holidays.service;

import com.rbc.holidays.dto.FileUploadResponse;
import com.rbc.holidays.dto.HolidayRequest;
import com.rbc.holidays.dto.HolidayResponse;
import com.rbc.holidays.enums.Country;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {

    HolidayResponse createHoliday(HolidayRequest request);

    HolidayResponse updateHoliday(Long id, HolidayRequest request);

    HolidayResponse getHolidayById(Long id);

    List<HolidayResponse> getAllHolidays();

    List<HolidayResponse> getHolidaysByCountry(Country country);

    List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);

    List<HolidayResponse> getHolidaysByCountryAndYear(Country country, Integer year);

    void deleteHoliday(Long id);

    FileUploadResponse uploadHolidaysFromFile(MultipartFile file);
}
