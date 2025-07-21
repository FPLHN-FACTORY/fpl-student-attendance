package udpm.hn.studentattendance.core.staff.student.attendancerecovery.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class STCreateOrUpdateNewEventRequestTest {
    @Test
    void coverage() {
        udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest req = new udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest();
        assertNotNull(req);
    }
}
