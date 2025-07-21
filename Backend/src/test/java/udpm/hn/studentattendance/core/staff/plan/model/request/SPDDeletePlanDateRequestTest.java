package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDDeletePlanDateRequestTest {
    @Test
    void coverage() {
        SPDDeletePlanDateRequest req = new SPDDeletePlanDateRequest();
        assertNotNull(req);
    }
}
