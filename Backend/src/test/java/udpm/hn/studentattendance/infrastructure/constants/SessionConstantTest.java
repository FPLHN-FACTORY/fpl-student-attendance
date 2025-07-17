package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(SessionConstant.LOGIN_ROLE);
        assertNotNull(SessionConstant.LOGIN_REDIRECT);
        assertNotNull(SessionConstant.LOGIN_FACILITY);
        assertNotNull(SessionConstant.AUTH_USER);
        assertFalse(SessionConstant.LOGIN_ROLE.isEmpty());
        assertFalse(SessionConstant.LOGIN_REDIRECT.isEmpty());
        assertFalse(SessionConstant.LOGIN_FACILITY.isEmpty());
        assertFalse(SessionConstant.AUTH_USER.isEmpty());
    }
}