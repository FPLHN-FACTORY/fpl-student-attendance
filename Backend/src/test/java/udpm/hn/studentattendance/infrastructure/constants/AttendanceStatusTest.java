package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatusTest {
    @Test
    void testAttendanceStatusExists() {
        assertNotNull(AttendanceStatus.PRESENT);
        assertNotNull(AttendanceStatus.ABSENT);
        assertNotNull(AttendanceStatus.CHECKIN);
        assertNotNull(AttendanceStatus.NOTCHECKIN);
    }

    @Test
    void testPresentStatus() {
        assertEquals("PRESENT", AttendanceStatus.PRESENT.name());
    }

    @Test
    void testAbsentStatus() {
        assertEquals("ABSENT", AttendanceStatus.ABSENT.name());
    }

    @Test
    void testCheckinStatus() {
        assertEquals("CHECKIN", AttendanceStatus.CHECKIN.name());
    }

    @Test
    void testNotCheckinStatus() {
        assertEquals("NOTCHECKIN", AttendanceStatus.NOTCHECKIN.name());
    }

    @Test
    void testValues() {
        AttendanceStatus[] values = AttendanceStatus.values();
        assertEquals(4, values.length);
        assertTrue(contains(values, AttendanceStatus.PRESENT));
        assertTrue(contains(values, AttendanceStatus.ABSENT));
        assertTrue(contains(values, AttendanceStatus.CHECKIN));
        assertTrue(contains(values, AttendanceStatus.NOTCHECKIN));
    }

    private boolean contains(AttendanceStatus[] values, AttendanceStatus status) {
        for (AttendanceStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}