package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IPTypeTest {
    @Test
    void testEnumValues() {
        for (IPType type : IPType.values()) {
            assertNotNull(type);
        }
    }
}