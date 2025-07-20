package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDModifyPlanDateAttendanceRequestTest {
    @Test
    void coverage() {
        SPDModifyPlanDateAttendanceRequest req = new SPDModifyPlanDateAttendanceRequest();
        assertNotNull(req);
    }
}
