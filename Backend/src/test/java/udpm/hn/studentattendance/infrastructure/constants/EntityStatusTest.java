package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityStatusTest {
    @Test
    void testEnumValues() {
        for (EntityStatus type : EntityStatus.values()) {
            assertNotNull(type);
        }
    }

    @Test
    void testEnumSpecificValues() {
        assertEquals(EntityStatus.ACTIVE, EntityStatus.valueOf("ACTIVE"));
        assertEquals(EntityStatus.INACTIVE, EntityStatus.valueOf("INACTIVE"));
    }

    @Test
    void testEnumToString() {
        assertEquals("ACTIVE", EntityStatus.ACTIVE.toString());
        assertEquals("INACTIVE", EntityStatus.INACTIVE.toString());
    }
}
