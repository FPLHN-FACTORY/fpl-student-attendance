package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImportLogTypeTest {
    @Test
    void testEnumValues() {
        for (ImportLogType type : ImportLogType.values()) {
            assertNotNull(type);
        }
    }
}