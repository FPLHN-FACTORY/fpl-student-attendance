package udpm.hn.studentattendance.core.staff.project.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class USProjectSearchRequestTest {
    @Test
    void coverage() {
        USProjectSearchRequest req = new USProjectSearchRequest();
        assertNotNull(req);
    }
}
