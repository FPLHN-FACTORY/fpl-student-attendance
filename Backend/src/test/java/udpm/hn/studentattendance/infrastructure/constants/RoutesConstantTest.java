package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoutesConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(RoutesConstant.PREFIX_API_ADMIN_MANAGEMENT);
        assertNotNull(RoutesConstant.PREFIX_API_STAFF_MANAGEMENT);
        assertNotNull(RoutesConstant.PREFIX_API_TEACHER_MANAGEMENT);
        assertNotNull(RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT);
        assertNotNull(RoutesConstant.PREFIX_API_EXCEL);
        assertNotNull(RoutesConstant.PREFIX_API_NOTIFICATION);
        assertFalse(RoutesConstant.PREFIX_API_ADMIN_MANAGEMENT.isEmpty());
        assertFalse(RoutesConstant.PREFIX_API_STAFF_MANAGEMENT.isEmpty());
        assertFalse(RoutesConstant.PREFIX_API_TEACHER_MANAGEMENT.isEmpty());
        assertFalse(RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT.isEmpty());
        assertFalse(RoutesConstant.PREFIX_API_EXCEL.isEmpty());
        assertFalse(RoutesConstant.PREFIX_API_NOTIFICATION.isEmpty());
    }
}
