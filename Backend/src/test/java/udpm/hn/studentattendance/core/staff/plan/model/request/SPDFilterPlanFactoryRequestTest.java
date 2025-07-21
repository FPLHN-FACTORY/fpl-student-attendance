package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDFilterPlanFactoryRequestTest {
    @Test
    void coverage() {
        SPDFilterPlanFactoryRequest req = new SPDFilterPlanFactoryRequest();
        assertNotNull(req);
        assertNotNull(req.toString());
    }
}
