package udpm.hn.studentattendance.infrastructure.common.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UALFilterRequestTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        UALFilterRequest req = new UALFilterRequest("f01", "u01", 1, "search", 100L, 200L);
        assertEquals("f01", req.getFacilityId());
        assertEquals("u01", req.getUserId());
        assertEquals(1, req.getRole());
        assertEquals("search", req.getSearchQuery());
        assertEquals(100L, req.getFromDate());
        assertEquals(200L, req.getToDate());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        UALFilterRequest req = new UALFilterRequest();
        req.setFacilityId("f02");
        req.setUserId("u02");
        req.setRole(2);
        req.setSearchQuery("find");
        req.setFromDate(111L);
        req.setToDate(222L);
        assertEquals("f02", req.getFacilityId());
        assertEquals("u02", req.getUserId());
        assertEquals(2, req.getRole());
        assertEquals("find", req.getSearchQuery());
        assertEquals(111L, req.getFromDate());
        assertEquals(222L, req.getToDate());
    }
}
