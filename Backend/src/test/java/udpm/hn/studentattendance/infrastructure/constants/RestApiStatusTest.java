package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestApiStatusTest {
    @Test
    void testEnumValues() {
        for (RestApiStatus type : RestApiStatus.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(RestApiStatus.SUCCESS, RestApiStatus.valueOf("SUCCESS"));
        assertEquals(RestApiStatus.ERROR, RestApiStatus.valueOf("ERROR"));
    }

    @Test
    void testEnumToString() {
        assertEquals("SUCCESS", RestApiStatus.SUCCESS.toString());
        assertEquals("ERROR", RestApiStatus.ERROR.toString());
    }
}
