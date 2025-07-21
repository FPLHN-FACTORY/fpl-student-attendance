package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterPlanDateAttendanceRequestTest {
    @Test
    void coverage() {
        SPDFilterPlanDateAttendanceRequest req = new SPDFilterPlanDateAttendanceRequest();
        assertNotNull(req);
        assertNotNull(req.toString());
    }
}
