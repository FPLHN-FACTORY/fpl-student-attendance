package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteExcelConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(RouteExcelConstant.DEFAULT_UPLOAD);
        assertNotNull(RouteExcelConstant.DEFAULT_IMPORT);
        assertNotNull(RouteExcelConstant.DEFAULT_DOWNLOAD_TEMPLATE);
        assertNotNull(RouteExcelConstant.DEFAULT_EXPORT);
        assertNotNull(RouteExcelConstant.DEFAULT_HISTORY_LOG);
        assertNotNull(RouteExcelConstant.URL_API_PLAN_DATE);
        assertNotNull(RouteExcelConstant.URL_API_STAFF);
        assertNotNull(RouteExcelConstant.URL_API_FACTORY);
        assertNotNull(RouteExcelConstant.URL_API_STUDENT);
        assertNotNull(RouteExcelConstant.URL_API_STUDENT_FACTORY);
        assertNotNull(RouteExcelConstant.URL_API_PROJECT);
        assertNotNull(RouteExcelConstant.URL_API_ATTENDANCE_RECOVERY);
    }
}