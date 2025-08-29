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
        // Test that toString contains the class name and field values
        assertTrue(toString.contains("STAttendanceRecoveryRequest"));
        assertTrue(toString.contains("page=2"));
        assertTrue(toString.contains("size=20"));
        assertTrue(toString.contains("orderBy=createdAt"));
        assertTrue(toString.contains("sortBy=desc"));
        assertTrue(toString.contains("q=findme"));
        assertTrue(toString.contains("searchQuery=abc"));
        assertTrue(toString.contains("fromDate=123456789"));
        assertTrue(toString.contains("toDate=987654321"));
        assertTrue(toString.contains("semesterId=sem-1"));
    }
}
