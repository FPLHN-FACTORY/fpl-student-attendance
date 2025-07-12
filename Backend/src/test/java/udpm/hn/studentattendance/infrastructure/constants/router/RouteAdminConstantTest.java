package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteAdminConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
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
} 