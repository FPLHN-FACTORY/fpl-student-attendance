package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Date;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    @DisplayName("Test constructor")
    void canInstantiate() {
        DateTimeUtils utils = new DateTimeUtils();
        assertThat(utils).isNotNull();
    }

    @Test
    @DisplayName("Test parseStringToLong with valid date")
    void testParseStringToLongWithValidDate() {
        String date = "25/12/2023";
        Long result = DateTimeUtils.parseStringToLong(date);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test parseStringToLong with custom format")
    void testParseStringToLongWithCustomFormat() {
        String date = "2023-12-25";
        String format = "yyyy-MM-dd";
        Long result = DateTimeUtils.parseStringToLong(date, format);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test parseStringToLong with numeric string")
    void testParseStringToLongWithNumericString() {
        String date = "1703462400000";
        Long result = DateTimeUtils.parseStringToLong(date);
        assertThat(result).isEqualTo(1703462400000L);
    }

    @Test
    @DisplayName("Test parseStringToLong with invalid date")
    void testParseStringToLongWithInvalidDate() {
        String date = "invalid-date";
        Long result = DateTimeUtils.parseStringToLong(date);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test parseStringToLong with null input")
    void testParseStringToLongWithNull() {
        Long result = DateTimeUtils.parseStringToLong(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test parseLongToString with valid timestamp")
    void testParseLongToStringWithValidTimestamp() {
        Long timestamp = 1703462400000L;
        String result = DateTimeUtils.parseLongToString(timestamp);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Test parseLongToString with custom format")
    void testParseLongToStringWithCustomFormat() {
        Long timestamp = 1703462400000L;
        String format = "yyyy-MM-dd";
        String result = DateTimeUtils.parseLongToString(timestamp, format);
        assertThat(result).isNotNull();
        assertThat(result).contains("2023");
    }

    @Test
    @DisplayName("Test parseLongToString with null input")
    void testParseLongToStringWithNull() {
        String result = DateTimeUtils.parseLongToString(null);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test convertDateToString with valid date")
    void testConvertDateToStringWithValidDate() {
        Date date = new Date(1703462400000L);
        String result = DateTimeUtils.convertDateToString(date);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Test convertDateToString with custom format")
    void testConvertDateToStringWithCustomFormat() {
        Date date = new Date(1703462400000L);
        String format = "yyyy-MM-dd";
        String result = DateTimeUtils.convertDateToString(date, format);
        assertThat(result).isNotNull();
        assertThat(result).contains("2023");
    }

    @Test
    @DisplayName("Test convertDateToString with null input")
    void testConvertDateToStringWithNull() {
        String result = DateTimeUtils.convertDateToString(null);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test convertMillisToDate with valid millis")
    void testConvertMillisToDateWithValidMillis() {
        long millis = 1703462400000L;
        String result = DateTimeUtils.convertMillisToDate(millis);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Test convertMillisToDate with custom format")
    void testConvertMillisToDateWithCustomFormat() {
        long millis = 1703462400000L;
        String format = "yyyy-MM-dd";
        String result = DateTimeUtils.convertMillisToDate(millis, format);
        assertThat(result).isNotNull();
        assertThat(result).contains("2023");
    }

    @Test
    @DisplayName("Test convertStringToTimeMillis with date only")
    void testConvertStringToTimeMillisWithDateOnly() {
        String dateString = "25/12/2023";
        long result = DateTimeUtils.convertStringToTimeMillis(dateString);
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test convertStringToTimeMillis with date and time")
    void testConvertStringToTimeMillisWithDateTime() {
        String dateString = "25/12/2023 10:30:00";
        String format = "dd/MM/yyyy HH:mm:ss";
        long result = DateTimeUtils.convertStringToTimeMillis(dateString, format);
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test convertStringToTimeMillis with custom format")
    void testConvertStringToTimeMillisWithCustomFormat() {
        String dateString = "2023-12-25";
        String format = "yyyy-MM-dd";
        long result = DateTimeUtils.convertStringToTimeMillis(dateString, format);
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test getCurrentTime")
    void testGetCurrentTime() {
        Long result = DateTimeUtils.getCurrentTime();
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test getCurrentTimeMillis")
    void testGetCurrentTimeMillis() {
        Long result = DateTimeUtils.getCurrentTimeMillis();
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test toStartOfDay")
    void testToStartOfDay() {
        long timestamp = 1703462400000L; // Some timestamp
        long result = DateTimeUtils.toStartOfDay(timestamp);
        assertThat(result).isGreaterThan(0L);
        assertThat(result).isLessThanOrEqualTo(timestamp);
    }

    @Test
    @DisplayName("Test toEndOfDay")
    void testToEndOfDay() {
        long timestamp = 1703462400000L; // Some timestamp
        long result = DateTimeUtils.toEndOfDay(timestamp);
        assertThat(result).isGreaterThan(0L);
        assertThat(result).isGreaterThan(timestamp);
    }

    @Test
    @DisplayName("Test getCurrentDateWithoutTime")
    void testGetCurrentDateWithoutTime() {
        Long result = DateTimeUtils.getCurrentDateWithoutTime();
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Test convertToLocalDate with valid date")
    void testConvertToLocalDateWithValidDate() {
        Date date = new Date(1703462400000L);
        LocalDate result = DateTimeUtils.convertToLocalDate(date);
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2023);
    }

    @Test
    @DisplayName("Test convertToLocalDate with null input")
    void testConvertToLocalDateWithNull() {
        LocalDate result = DateTimeUtils.convertToLocalDate(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test convertTimestampToLocalDate")
    void testConvertTimestampToLocalDate() {
        long timestamp = 1703462400000L;
        LocalDate result = DateTimeUtils.convertTimestampToLocalDate(timestamp);
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2023);
    }

    @Test
    @DisplayName("Test edge case with very large timestamp")
    void testEdgeCaseWithVeryLargeTimestamp() {
        long largeTimestamp = Long.MAX_VALUE;
        LocalDate result = DateTimeUtils.convertTimestampToLocalDate(largeTimestamp);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test edge case with negative timestamp")
    void testEdgeCaseWithNegativeTimestamp() {
        long negativeTimestamp = -1000L;
        LocalDate result = DateTimeUtils.convertTimestampToLocalDate(negativeTimestamp);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test parseStringToLong with empty string")
    void testParseStringToLongWithEmptyString() {
        String emptyDate = "";
        Long result = DateTimeUtils.parseStringToLong(emptyDate);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test parseStringToLong with whitespace string")
    void testParseStringToLongWithWhitespaceString() {
        String whitespaceDate = "   ";
        Long result = DateTimeUtils.parseStringToLong(whitespaceDate);
        assertThat(result).isNull();
    }
}