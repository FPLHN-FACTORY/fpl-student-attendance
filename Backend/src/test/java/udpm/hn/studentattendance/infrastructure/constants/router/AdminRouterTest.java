package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminRouterTest {
    @Test
    void testRouteAdminConstantExists() {
        assertNotNull(RouteAdminConstant.URL_API_SUBJECT_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_SETTINGS);
        assertNotNull(RouteAdminConstant.URL_API_LEVEL_PROJECT_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_SEMESTER_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_STAFF_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_ADMIN_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_USER_ACTIVITY_MANAGEMENT);
        assertNotNull(RouteAdminConstant.URL_API_STATISTICS_MANAGEMENT);
    }

    @Test
    void testSubjectManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_SUBJECT_MANAGEMENT.contains("/subject-management"));
    }

    @Test
    void testSettingsPath() {
        assertEquals("/admin/settings", RouteAdminConstant.URL_API_SETTINGS);
    }

    @Test
    void testLevelProjectManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_LEVEL_PROJECT_MANAGEMENT.contains("/level-project-management"));
    }

    @Test
    void testSubjectFacilityManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT.contains("/subject-facility-management"));
    }

    @Test
    void testFacilityManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT.contains("/facilities"));
    }

    @Test
    void testSemesterManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_SEMESTER_MANAGEMENT.contains("/semesters"));
    }

    @Test
    void testStaffManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_STAFF_MANAGEMENT.contains("/staff-management"));
    }

    @Test
    void testAdminManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_ADMIN_MANAGEMENT.contains("/admin-management"));
    }

    @Test
    void testUserActivityManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_USER_ACTIVITY_MANAGEMENT.contains("/user-activity-management"));
    }

    @Test
    void testStatisticsManagementPath() {
        assertTrue(RouteAdminConstant.URL_API_STATISTICS_MANAGEMENT.contains("/statistics-management"));
    }
}