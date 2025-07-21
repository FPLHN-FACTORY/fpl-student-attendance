package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDAddOrUpdatePlanRequestTest {
    @Test
    void coverage() {
        SPDAddOrUpdatePlanRequest req = new SPDAddOrUpdatePlanRequest();
        assertNotNull(req);
    }
}
