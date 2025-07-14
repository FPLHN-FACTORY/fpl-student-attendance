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
}