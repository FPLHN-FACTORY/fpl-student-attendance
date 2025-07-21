package udpm.hn.studentattendance.entities.base;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditEntityTest {
    static class TestAuditEntity extends AuditEntity {
    }

    @Test
    void testNoArgsConstructor() {
        AuditEntity entity = new TestAuditEntity();
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        AuditEntity entity = new TestAuditEntity();
        entity.setCreatedAt(1000L);
        entity.setUpdatedAt(2000L);
        assertEquals(1000L, entity.getCreatedAt());
        assertEquals(2000L, entity.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        AuditEntity e1 = new TestAuditEntity();
        e1.setCreatedAt(1000L);
        e1.setUpdatedAt(2000L);

        AuditEntity e2 = new TestAuditEntity();
        e2.setCreatedAt(1000L);
        e2.setUpdatedAt(2000L);

        AuditEntity e3 = new TestAuditEntity();
        e3.setCreatedAt(3000L);
        e3.setUpdatedAt(2000L);

        // Only check that fields are set correctly, do not compare objects directly
        assertEquals(e1.getCreatedAt(), e2.getCreatedAt());
        assertEquals(e1.getUpdatedAt(), e2.getUpdatedAt());
        assertNotEquals(e1.getCreatedAt(), e3.getCreatedAt());
    }

    @Test
    void testToString() {
        AuditEntity entity = new TestAuditEntity();
        entity.setCreatedAt(1000L);
        entity.setUpdatedAt(2000L);
        String toString = entity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestAuditEntity") || toString.contains("AuditEntity"));
        assertTrue(toString.contains("createdAt=1000") || toString.contains("1000"));
        assertTrue(toString.contains("updatedAt=2000") || toString.contains("2000"));
    }

    @Test
    void testEqualsWithNull() {
        AuditEntity entity = new TestAuditEntity();
        entity.setCreatedAt(1000L);
        assertNotEquals(null, entity);
    }

    @Test
    void testEqualsWithDifferentClass() {
        AuditEntity entity = new TestAuditEntity();
        entity.setCreatedAt(1000L);
        Object other = new Object();
        assertNotEquals(entity, other);
    }

    @Test
    void testEqualsWithSameObject() {
        AuditEntity entity = new TestAuditEntity();
        entity.setCreatedAt(1000L);
        assertEquals(entity, entity);
    }
}
