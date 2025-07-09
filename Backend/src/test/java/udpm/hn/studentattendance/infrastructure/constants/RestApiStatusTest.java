package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestApiStatusTest {
    @Test
    void testEnumValues() {
        for (RestApiStatus status : RestApiStatus.values()) {
            assertNotNull(status);
        }
    }
}