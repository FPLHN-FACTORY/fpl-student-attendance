package udpm.hn.studentattendance.infrastructure.security.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RedisSecurityExceptionTest {
    @Test
    void testClassExists() {
        assertNotNull(new RedisSecurityException("Test message"));
    }
}
