package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STCreateOrUpdateNewEventRequestTest {
    @Test
    void testGetterSetter() {
        STCreateOrUpdateNewEventRequest req = new STCreateOrUpdateNewEventRequest();
        req.setName("Hoạt động A");
        req.setDay(123456789L);
        req.setDescription("Mô tả hoạt động");

        assertEquals("Hoạt động A", req.getName());
        assertEquals(123456789L, req.getDay());
        assertEquals("Mô tả hoạt động", req.getDescription());
    }
}
