package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PageableRequestTest {
    static class TestPageableRequest extends PageableRequest {
    }

    @Test
    void testDefaultValuesAndSetters() {
        TestPageableRequest req = new TestPageableRequest();
        // Test default values
        assertEquals(1, req.getPage());
        assertEquals(5, req.getSize());
        assertEquals("desc", req.getOrderBy());
        assertEquals("id", req.getSortBy());
        assertNull(req.getQ());
        // Test setters
        req.setPage(2);
        req.setSize(50);
        req.setOrderBy("name");
        req.setSortBy("asc");
        req.setQ("search");
        assertEquals(2, req.getPage());
        assertEquals(50, req.getSize());
        assertEquals("name", req.getOrderBy());
        assertEquals("asc", req.getSortBy());
        assertEquals("search", req.getQ());
    }
}
