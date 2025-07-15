package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STAttendanceRecoveryRequestTest {
    @Test
    void testGetterSetterAndToString() {
        STAttendanceRecoveryRequest req = new STAttendanceRecoveryRequest();
        req.setSearchQuery("abc");
        req.setFromDate(123456789L);
        req.setToDate(987654321L);
        req.setSemesterId("sem-1");
        req.setPage(2);
        req.setSize(20);
        req.setOrderBy("createdAt");
        req.setSortBy("desc");
        req.setQ("findme");

        assertEquals("abc", req.getSearchQuery());
        assertEquals(123456789L, req.getFromDate());
        assertEquals(987654321L, req.getToDate());
        assertEquals("sem-1", req.getSemesterId());
        assertEquals(2, req.getPage());
        assertEquals(20, req.getSize());
        assertEquals("createdAt", req.getOrderBy());
        assertEquals("desc", req.getSortBy());
        assertEquals("findme", req.getQ());

        String toString = req.toString();
        assertTrue(toString.contains("page=2"));
        assertTrue(toString.contains("_size=20"));
        assertTrue(toString.contains("_orderBy=createdAt"));
        assertTrue(toString.contains("_sortBy=desc"));
        assertTrue(toString.contains("_q=findme"));
        assertTrue(toString.contains("_searchQuery=abc"));
        assertTrue(toString.contains("_fromDate=123456789"));
        assertTrue(toString.contains("_toDate=987654321"));
        assertTrue(toString.contains("_semesterId=sem-1"));
    }
}