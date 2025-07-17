package udpm.hn.studentattendance.infrastructure.config;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.config.application.WebMvcConfig;
import static org.junit.jupiter.api.Assertions.*;

class WebMvcConfigTest {
    @Test
    void testClassExists() {
        assertNotNull(new WebMvcConfig());
    }
}