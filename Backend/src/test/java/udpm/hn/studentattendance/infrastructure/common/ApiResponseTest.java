package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

class ApiResponseTest {
    @Test
    void testSuccessStaticMethods() {
        ApiResponse res1 = ApiResponse.success("OK");
        assertEquals(RestApiStatus.SUCCESS, res1.getStatus());
        assertEquals("OK", res1.getMessage());
        assertNull(res1.getData());

        ApiResponse res2 = ApiResponse.success("OK", 123);
        assertEquals(RestApiStatus.SUCCESS, res2.getStatus());
        assertEquals("OK", res2.getMessage());
        assertEquals(123, res2.getData());
    }

    @Test
    void testErrorStaticMethods() {
        ApiResponse res1 = ApiResponse.error("ERR");
        assertEquals(RestApiStatus.ERROR, res1.getStatus());
        assertEquals("ERR", res1.getMessage());
        assertNull(res1.getData());

        ApiResponse res2 = ApiResponse.error("ERR", "data");
        assertEquals(RestApiStatus.ERROR, res2.getStatus());
        assertEquals("ERR", res2.getMessage());
        assertEquals("data", res2.getData());
    }

    @Test
    void testWarningStaticMethods() {
        ApiResponse res1 = ApiResponse.warning("WARN");
        assertEquals(RestApiStatus.WARNING, res1.getStatus());
        assertEquals("WARN", res1.getMessage());
        assertNull(res1.getData());

        ApiResponse res2 = ApiResponse.warning("WARN", 456);
        assertEquals(RestApiStatus.WARNING, res2.getStatus());
        assertEquals("WARN", res2.getMessage());
        assertEquals(456, res2.getData());
    }

    @Test
    void testPendingStaticMethods() {
        ApiResponse res1 = ApiResponse.pending("PENDING");
        assertEquals(RestApiStatus.PENDING, res1.getStatus());
        assertEquals("PENDING", res1.getMessage());
        assertNull(res1.getData());

        ApiResponse res2 = ApiResponse.pending("PENDING", "wait");
        assertEquals(RestApiStatus.PENDING, res2.getStatus());
        assertEquals("PENDING", res2.getMessage());
        assertEquals("wait", res2.getData());
    }

    @Test
    void testConstructors() {
        ApiResponse res1 = new ApiResponse("msg", 789);
        assertEquals(RestApiStatus.SUCCESS, res1.getStatus());
        assertEquals("msg", res1.getMessage());
        assertEquals(789, res1.getData());

        ApiResponse res2 = new ApiResponse(RestApiStatus.ERROR, "err", 111);
        assertEquals(RestApiStatus.ERROR, res2.getStatus());
        assertEquals("err", res2.getMessage());
        assertEquals(111, res2.getData());
    }
}
