package com.rbc.holidays.util;

import com.rbc.holidays.dto.HolidayRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileParserUtilTest {

    @Test
    void parseCsvFileShouldParseRecords() throws Exception {
        String csv = "holidayName,holidayDate,country,isRecurring,description\nIndependence Day,2026-07-04,usa,true,Federal holiday";
        MockMultipartFile file = new MockMultipartFile("file", "holidays.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        List<HolidayRequest> requests = FileParserUtil.parseCsvFile(file);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0)).isEqualTo(new HolidayRequest(
                "Independence Day",
                LocalDate.of(2026, 7, 4),
                "usa",
                true,
                "Federal holiday"
        ));
    }

    @Test
    void parseJsonFileShouldParseRecords() throws Exception {
        String json = "[{\"holidayName\":\"Canada Day\",\"holidayDate\":\"2026-07-01\",\"country\":\"CANADA\",\"isRecurring\":true,\"description\":\"National holiday\"}]";
        MockMultipartFile file = new MockMultipartFile("file", "holidays.json", "application/json", json.getBytes(StandardCharsets.UTF_8));

        List<HolidayRequest> requests = FileParserUtil.parseJsonFile(file);

        assertThat(requests).containsExactly(new HolidayRequest(
                "Canada Day",
                LocalDate.of(2026, 7, 1),
                "CANADA",
                true,
                "National holiday"
        ));
    }

    @Test
    void constructorShouldPreventInstantiation() throws Exception {
        Constructor<FileParserUtil> constructor = FileParserUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Utility class - do not instantiate");
    }
}