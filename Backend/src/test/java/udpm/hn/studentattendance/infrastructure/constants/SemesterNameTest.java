package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemesterNameTest {
    @Test
    void testEnumValues() {
        for (SemesterName name : SemesterName.values()) {
            assertNotNull(name);
        }
    }
}