package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityStatusTest {
    @Test
    void testEntityStatusExists() {
        assertNotNull(EntityStatus.ACTIVE);
        assertNotNull(EntityStatus.INACTIVE);
    }

    @Test
    void testActiveStatus() {
        assertEquals("ACTIVE", EntityStatus.ACTIVE.name());
    }

    @Test
    void testInactiveStatus() {
        assertEquals("INACTIVE", EntityStatus.INACTIVE.name());
    }

    @Test
    void testFromValue() {
        assertEquals(EntityStatus.INACTIVE, EntityStatus.fromValue(0));
        assertEquals(EntityStatus.ACTIVE, EntityStatus.fromValue(1));
        assertNull(EntityStatus.fromValue(null));
    }

    @Test
    void testValues() {
        EntityStatus[] values = EntityStatus.values();
        assertEquals(2, values.length);
        assertTrue(contains(values, EntityStatus.ACTIVE));
        assertTrue(contains(values, EntityStatus.INACTIVE));
    }

    private boolean contains(EntityStatus[] values, EntityStatus status) {
        for (EntityStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}