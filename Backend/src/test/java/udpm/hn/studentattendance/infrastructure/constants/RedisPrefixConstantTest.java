package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedisPrefixConstantTest {
    @Test
    void coverage() {
        assertNotNull(RedisPrefixConstant.class);
    }
}
