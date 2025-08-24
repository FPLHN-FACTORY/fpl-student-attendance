package udpm.hn.studentattendance.core.staff.studentfactory.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class USStudentFactoryAddRequestTest {
    @Test
    void coverage() {
        USStudentFactoryAddRequest req = new USStudentFactoryAddRequest();
        assertNotNull(req);
    }
}
