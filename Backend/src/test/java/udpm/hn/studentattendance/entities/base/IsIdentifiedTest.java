package udpm.hn.studentattendance.entities.base;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsIdentifiedTest {
    @Test
    void testIsIdentifiedInterface() {
        // Test that IsIdentified is just an interface marker
        // No actual methods to test, just verify the interface exists
        assertNotNull(IsIdentified.class);
        assertTrue(IsIdentified.class.isInterface());
    }
}