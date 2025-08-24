package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleConstantTest {
    @Test
    void testEnumValues() {
        for (RoleConstant type : RoleConstant.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(RoleConstant.ADMIN, RoleConstant.valueOf("ADMIN"));
        assertEquals(RoleConstant.STAFF, RoleConstant.valueOf("STAFF"));
        assertEquals(RoleConstant.TEACHER, RoleConstant.valueOf("TEACHER"));
        assertEquals(RoleConstant.STUDENT, RoleConstant.valueOf("STUDENT"));
    }

    @Test
    void testEnumToString() {
        assertEquals("ADMIN", RoleConstant.ADMIN.toString());
        assertEquals("STAFF", RoleConstant.STAFF.toString());
        assertEquals("TEACHER", RoleConstant.TEACHER.toString());
        assertEquals("STUDENT", RoleConstant.STUDENT.toString());
    }
}
