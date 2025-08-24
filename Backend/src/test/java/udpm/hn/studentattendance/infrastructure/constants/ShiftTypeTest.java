package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShiftTypeTest {
    @Test
    void testEnumValues() {
        for (ShiftType type : ShiftType.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(ShiftType.OFFLINE, ShiftType.valueOf("OFFLINE"));
        assertEquals(ShiftType.ONLINE, ShiftType.valueOf("ONLINE"));
    }

    @Test
    void testEnumToString() {
        assertEquals("OFFLINE", ShiftType.OFFLINE.toString());
        assertEquals("ONLINE", ShiftType.ONLINE.toString());
    }
}
