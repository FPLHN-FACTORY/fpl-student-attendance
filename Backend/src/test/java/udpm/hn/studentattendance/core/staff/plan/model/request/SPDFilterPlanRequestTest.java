package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterPlanRequestTest {
    @Test
    void coverage() {
        SPDFilterPlanRequest req = new SPDFilterPlanRequest();
        assertNotNull(req);
        assertNotNull(req.toString());
    }
}