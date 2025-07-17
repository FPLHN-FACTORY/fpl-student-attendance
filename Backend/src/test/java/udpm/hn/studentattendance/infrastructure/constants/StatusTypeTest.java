package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusTypeTest {
    @Test
    void testEnumValues() {
        for (StatusType type : StatusType.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(StatusType.DISABLE, StatusType.valueOf("DISABLE"));
        assertEquals(StatusType.ENABLE, StatusType.valueOf("ENABLE"));
    }

    @Test
    void testEnumToString() {
        assertEquals("DISABLE", StatusType.DISABLE.toString());
        assertEquals("ENABLE", StatusType.ENABLE.toString());
    }
}