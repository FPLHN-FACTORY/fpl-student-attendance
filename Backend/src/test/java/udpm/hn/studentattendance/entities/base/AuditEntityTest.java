package udpm.hn.studentattendance.entities.base;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditEntityTest {
    @Test
    void testNoArgsConstructor() {
        AuditEntity entity = new AuditEntity() {
        };
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        AuditEntity entity = new AuditEntity() {
        };
        entity.setCreatedAt(1000L);
        entity.setUpdatedAt(2000L);
        assertEquals(1000L, entity.getCreatedAt());
        assertEquals(2000L, entity.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        AuditEntity e1 = new AuditEntity() {
        };
        e1.setCreatedAt(1000L);
        e1.setUpdatedAt(2000L);

        AuditEntity e2 = new AuditEntity() {
        };
        e2.setCreatedAt(1000L);
        e2.setUpdatedAt(2000L);

        AuditEntity e3 = new AuditEntity() {
        };
        e3.setCreatedAt(3000L);
        e3.setUpdatedAt(2000L);

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1.hashCode(), e3.hashCode());
    }

    @Test
    void testToString() {
        AuditEntity entity = new AuditEntity() {
        };
        entity.setCreatedAt(1000L);
        entity.setUpdatedAt(2000L);
        String toString = entity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AuditEntity"));
        assertTrue(toString.contains("createdAt=1000"));
        assertTrue(toString.contains("updatedAt=2000"));
    }

    @Test
    void testEqualsWithNull() {
        AuditEntity entity = new AuditEntity() {
        };
        entity.setCreatedAt(1000L);
        assertNotEquals(null, entity);
    }

    @Test
    void testEqualsWithDifferentClass() {
        AuditEntity entity = new AuditEntity() {
        };
        entity.setCreatedAt(1000L);
        Object other = new Object();
        assertNotEquals(entity, other);
    }

    @Test
    void testEqualsWithSameObject() {
        AuditEntity entity = new AuditEntity() {
        };
        entity.setCreatedAt(1000L);
        assertEquals(entity, entity);
    }
}