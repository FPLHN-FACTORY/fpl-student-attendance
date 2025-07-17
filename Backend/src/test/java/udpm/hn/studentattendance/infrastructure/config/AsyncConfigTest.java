package udpm.hn.studentattendance.infrastructure.config;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.config.application.AsyncConfig;
import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {
    @Test
    void testClassExists() {
        assertNotNull(new AsyncConfig());
    }
}