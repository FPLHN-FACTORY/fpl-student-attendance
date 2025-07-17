package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteStudentConstantTest {
    @Test
    void testRouteStudentConstantExists() {
        assertNotNull(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT);
        assertNotNull(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_SCHEDULE_MANAGEMENT);
        assertNotNull(RouteStudentConstant.URL_API_ATTENDANCE);
        assertNotNull(RouteStudentConstant.URL_API_STATISTICS);
    }

    @Test
    void testAttendanceHistoryPath() {
        assertTrue(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT.contains("/attendance-history"));
    }

    @Test
    void testAttendanceSchedulePath() {
        assertTrue(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_SCHEDULE_MANAGEMENT.contains("/plan-attendance"));
    }

    @Test
    void testAttendancePath() {
        assertTrue(RouteStudentConstant.URL_API_ATTENDANCE.contains("/attendance"));
    }

    @Test
    void testStatisticsPath() {
        assertTrue(RouteStudentConstant.URL_API_STATISTICS.contains("/statistics"));
    }
} 