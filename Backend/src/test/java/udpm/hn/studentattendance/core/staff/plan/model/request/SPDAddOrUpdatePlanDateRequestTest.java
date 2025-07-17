package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDAddOrUpdatePlanDateRequestTest {
    @Test
    void coverage() {
        SPDAddOrUpdatePlanDateRequest req = new SPDAddOrUpdatePlanDateRequest();
        assertNotNull(req);
    }
}