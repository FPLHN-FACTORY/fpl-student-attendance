package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatusTest {
    @Test
    void testEnumValues() {
        for (AttendanceStatus type : AttendanceStatus.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(AttendanceStatus.NOTCHECKIN, AttendanceStatus.valueOf("NOTCHECKIN"));
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.valueOf("ABSENT"));
        assertEquals(AttendanceStatus.CHECKIN, AttendanceStatus.valueOf("CHECKIN"));
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.valueOf("PRESENT"));
    }

    @Test
    void testEnumToString() {
        assertEquals("NOTCHECKIN", AttendanceStatus.NOTCHECKIN.toString());
        assertEquals("ABSENT", AttendanceStatus.ABSENT.toString());
        assertEquals("CHECKIN", AttendanceStatus.CHECKIN.toString());
        assertEquals("PRESENT", AttendanceStatus.PRESENT.toString());
    }
}
