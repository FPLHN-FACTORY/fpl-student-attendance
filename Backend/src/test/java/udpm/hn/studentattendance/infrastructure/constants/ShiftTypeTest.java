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
}