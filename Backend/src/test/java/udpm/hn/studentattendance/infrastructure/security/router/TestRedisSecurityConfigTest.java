package udpm.hn.studentattendance.infrastructure.security.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestRedisSecurityConfigTest {
    @Test
    void testClassExists() {
        assertNotNull(new TestRedisSecurityConfig());
    }
}