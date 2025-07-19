package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteTeacherConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(RouteTeacherConstant.URL_API_STATISTICS_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_FACTORY_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_STUDENT_FACTORY_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_SHIFT_FACTORY_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_PLANDATE_ATTENDANCE_FACTORY_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_TEACHING_SCHEDULE_MANAGEMENT);
        assertNotNull(RouteTeacherConstant.URL_API_STUDENT_ATTENDANCE);
    }
}