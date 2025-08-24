package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterCreatePlanDateRequestTest {
    @Test
    void coverage() {
        SPDFilterCreatePlanDateRequest req = new SPDFilterCreatePlanDateRequest();
        assertNotNull(req);
    }
}
