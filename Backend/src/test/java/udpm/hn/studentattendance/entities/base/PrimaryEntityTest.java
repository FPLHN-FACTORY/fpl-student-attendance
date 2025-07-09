package udpm.hn.studentattendance.entities.base;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import static org.junit.jupiter.api.Assertions.*;

class PrimaryEntityTest {
    @Test
    void testNoArgsConstructor() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        assertNull(entity.getId());
        assertNull(entity.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        entity.setStatus(EntityStatus.ACTIVE);
        assertEquals("1", entity.getId());
        assertEquals(EntityStatus.ACTIVE, entity.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        entity.setStatus(EntityStatus.ACTIVE);
        assertEquals("1", entity.getId());
        assertEquals(EntityStatus.ACTIVE, entity.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        PrimaryEntity e1 = new PrimaryEntity() {
        };
        e1.setId("1");
        e1.setStatus(EntityStatus.ACTIVE);

        PrimaryEntity e2 = new PrimaryEntity() {
        };
        e2.setId("1");
        e2.setStatus(EntityStatus.ACTIVE);

        PrimaryEntity e3 = new PrimaryEntity() {
        };
        e3.setId("2");
        e3.setStatus(EntityStatus.ACTIVE);

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1.hashCode(), e3.hashCode());
    }

    @Test
    void testToString() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        entity.setStatus(EntityStatus.ACTIVE);
        String toString = entity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PrimaryEntity"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("status=ACTIVE"));
    }

    @Test
    void testEqualsWithNull() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        assertNotEquals(null, entity);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        Object other = new Object();
        assertNotEquals(entity, other);
    }

    @Test
    void testEqualsWithSameObject() {
        PrimaryEntity entity = new PrimaryEntity() {
        };
        entity.setId("1");
        assertEquals(entity, entity);
    }
}