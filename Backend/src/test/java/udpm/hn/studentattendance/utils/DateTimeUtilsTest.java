package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {
    @Test
    void testParseStringToLongAndBack() {
        String dateStr = "01/01/2020";
        Long millis = DateTimeUtils.parseStringToLong(dateStr);
        assertNotNull(millis);
        String back = DateTimeUtils.parseLongToString(millis);
        assertEquals(dateStr, back);
    }

    @Test
    void testParseStringToLong_invalid() {
        assertNull(DateTimeUtils.parseStringToLong("not-a-date"));
    }

    @Test
    void testConvertDateToString() {
        Date date = new Date(1577836800000L); // 01/01/2020
        String str = DateTimeUtils.convertDateToString(date);
        assertEquals("01/01/2020", str);
    }

    @Test
    void testConvertMillisToDate() {
        long millis = 1577836800000L;
        String str = DateTimeUtils.convertMillisToDate(millis);
        assertEquals("01/01/2020", str);
    }

    @Test
    void testConvertStringToTimeMillis() {
        String dateStr = "01/01/2020";
        long millis = DateTimeUtils.convertStringToTimeMillis(dateStr);
        assertEquals(1577811600000L, millis); // Asia/Ho_Chi_Minh start of day
    }

    @Test
    void testGetCurrentTime() {
        Long now = DateTimeUtils.getCurrentTime();
        assertNotNull(now);
        assertTrue(now > 0);
    }

    @Test
    void testToStartOfDayAndEndOfDay() {
        long millis = 1577836800000L; // 01/01/2020
        long start = DateTimeUtils.toStartOfDay(millis);
        long end = DateTimeUtils.toEndOfDay(millis);
        assertTrue(start < end);
    }

    @Test
    void testGetCurrentDateWithoutTime() {
        Long millis = DateTimeUtils.getCurrentDateWithoutTime();
        assertNotNull(millis);
    }

    @Test
    void testConvertToLocalDate() {
        Date date = new Date(1577836800000L);
        LocalDate localDate = DateTimeUtils.convertToLocalDate(date);
        assertEquals(LocalDate.of(2020, 1, 1), localDate);
    }

    @Test
    void testConvertTimestampToLocalDate() {
        long millis = 1577836800000L;
        LocalDate localDate = DateTimeUtils.convertTimestampToLocalDate(millis);
        assertEquals(LocalDate.of(2020, 1, 1), localDate);
    }
}
