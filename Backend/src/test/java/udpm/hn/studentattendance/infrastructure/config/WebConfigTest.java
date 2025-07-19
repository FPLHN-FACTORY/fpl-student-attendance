package udpm.hn.studentattendance.infrastructure.config;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.config.application.WebConfig;
import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {
    @Test
    void testClassExists() {
        assertNotNull(new WebConfig());
    }
}