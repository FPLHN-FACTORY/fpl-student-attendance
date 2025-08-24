package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemesterNameTest {
    @Test
    void testEnumValues() {
        for (SemesterName type : SemesterName.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(SemesterName.FALL, SemesterName.valueOf("FALL"));
        assertEquals(SemesterName.SPRING, SemesterName.valueOf("SPRING"));
        assertEquals(SemesterName.SUMMER, SemesterName.valueOf("SUMMER"));
    }

    @Test
    void testEnumToString() {
        assertEquals("FALL", SemesterName.FALL.toString());
        assertEquals("SPRING", SemesterName.SPRING.toString());
        assertEquals("SUMMER", SemesterName.SUMMER.toString());
    }
}
