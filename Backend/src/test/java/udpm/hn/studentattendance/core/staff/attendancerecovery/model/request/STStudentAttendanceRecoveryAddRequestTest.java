package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STStudentAttendanceRecoveryAddRequestTest {
    @Test
    void testGetterSetter() {
        STStudentAttendanceRecoveryAddRequest req = new STStudentAttendanceRecoveryAddRequest();
        req.setDay(123L);
        req.setStudentCode("SV001");
        req.setAttendanceRecoveryId("ar-1");

        assertEquals(123L, req.getDay());
        assertEquals("SV001", req.getStudentCode());
        assertEquals("ar-1", req.getAttendanceRecoveryId());
    }
}
