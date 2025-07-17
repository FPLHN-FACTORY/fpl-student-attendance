package udpm.hn.studentattendance.core.staff.plan.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPDAddPlanFactoryRequestTest {
    @Test
    void coverage() {
        SPDAddPlanFactoryRequest req = new SPDAddPlanFactoryRequest();
        assertNotNull(req);
    }
}