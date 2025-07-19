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

    @Test
    void testEnumSpecificValues() {
        assertEquals(ImportLogType.PLAN_DATE, ImportLogType.valueOf("PLAN_DATE"));
        assertEquals(ImportLogType.STAFF, ImportLogType.valueOf("STAFF"));
        assertEquals(ImportLogType.FACTORY, ImportLogType.valueOf("FACTORY"));
        assertEquals(ImportLogType.STUDENT, ImportLogType.valueOf("STUDENT"));
        assertEquals(ImportLogType.STUDENT_FACTORY, ImportLogType.valueOf("STUDENT_FACTORY"));
        assertEquals(ImportLogType.PROJECT, ImportLogType.valueOf("PROJECT"));
        assertEquals(ImportLogType.ATTENDANCE_RECOVERY, ImportLogType.valueOf("ATTENDANCE_RECOVERY"));
    }

    @Test
    void testEnumToString() {
        assertEquals("PLAN_DATE", ImportLogType.PLAN_DATE.toString());
        assertEquals("STAFF", ImportLogType.STAFF.toString());
        assertEquals("FACTORY", ImportLogType.FACTORY.toString());
        assertEquals("STUDENT", ImportLogType.STUDENT.toString());
        assertEquals("STUDENT_FACTORY", ImportLogType.STUDENT_FACTORY.toString());
        assertEquals("PROJECT", ImportLogType.PROJECT.toString());
        assertEquals("ATTENDANCE_RECOVERY", ImportLogType.ATTENDANCE_RECOVERY.toString());
    }
}