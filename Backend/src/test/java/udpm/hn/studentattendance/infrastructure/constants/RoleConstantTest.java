package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleConstantTest {
    @Test
    void testEnumValues() {
        for (RoleConstant role : RoleConstant.values()) {
            assertNotNull(role);
        }
    }
} 