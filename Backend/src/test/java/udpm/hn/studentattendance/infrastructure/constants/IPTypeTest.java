package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IPTypeTest {
    @Test
    void testEnumValues() {
        for (IPType type : IPType.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(IPType.IPV4, IPType.valueOf("IPV4"));
        assertEquals(IPType.IPV6, IPType.valueOf("IPV6"));
        assertEquals(IPType.DNSSUFFIX, IPType.valueOf("DNSSUFFIX"));
    }

    @Test
    void testEnumToString() {
        assertEquals("IPV4", IPType.IPV4.toString());
        assertEquals("IPV6", IPType.IPV6.toString());
        assertEquals("DNSSUFFIX", IPType.DNSSUFFIX.toString());
    }
}