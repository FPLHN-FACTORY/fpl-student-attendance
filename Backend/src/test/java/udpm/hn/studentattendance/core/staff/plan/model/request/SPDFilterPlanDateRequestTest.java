package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterPlanDateRequestTest {
    @Test
    void coverage() {
        SPDFilterPlanDateRequest req = new SPDFilterPlanDateRequest();
        assertNotNull(req);
        assertNotNull(req.toString());
    }
}
