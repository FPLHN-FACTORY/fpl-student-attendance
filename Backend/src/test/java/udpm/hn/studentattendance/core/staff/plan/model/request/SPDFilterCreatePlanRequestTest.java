package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterCreatePlanRequestTest {
    @Test
    void coverage() {
        SPDFilterCreatePlanRequest req = new SPDFilterCreatePlanRequest();
        assertNotNull(req);
    }
}