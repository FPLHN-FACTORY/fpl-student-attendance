package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STCreateOrUpdateNewEventRequestTest {
    @Test
    void testGetterSetter() {
        STCreateOrUpdateNewEventRequest req = new STCreateOrUpdateNewEventRequest();
        req.setName("Sự kiện A");
        req.setDay(123456789L);
        req.setDescription("Mô tả sự kiện");

        assertEquals("Sự kiện A", req.getName());
        assertEquals(123456789L, req.getDay());
        assertEquals("Mô tả sự kiện", req.getDescription());
    }
}
