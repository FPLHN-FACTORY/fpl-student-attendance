package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppUtilsTest {
    @Test
    void testImageUrlToBase64_invalidUrl() {
        // Trả về chính url nếu lỗi
        String url = "http://invalid-url";
        String result = AppUtils.imageUrlToBase64(url);
        assertEquals(url, result);
    }

    @Test
    void testGetDnsSuffixes() {
        List<String> suffixes = AppUtils.getDnsSuffixes();
        assertNotNull(suffixes);
        assertTrue(suffixes instanceof List);
    }

    @Test
    void testGetPublicIP() {
        // Có thể null nếu không có mạng, chỉ kiểm tra không ném exception
        try {
            String ip = AppUtils.getPublicIP();
            // Có thể null hoặc là chuỗi IP
            assertTrue(ip == null || ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"));
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    // getClientIP cần mock HttpServletRequest, nên không test ở đây
}
