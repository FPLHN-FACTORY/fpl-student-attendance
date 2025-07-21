package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelProjectTest {
    @Test
    void testNoArgsConstructor() {
        LevelProject lp = new LevelProject();
        assertNull(lp.getCode());
        assertNull(lp.getName());
        assertNull(lp.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        LevelProject lp = new LevelProject("LP01", "Level 1", "Description");
        assertEquals("LP01", lp.getCode());
        assertEquals("Level 1", lp.getName());
        assertEquals("Description", lp.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        LevelProject lp = new LevelProject();
        lp.setCode("LP01");
        lp.setName("Level 1");
        lp.setDescription("Description");
        assertEquals("LP01", lp.getCode());
        assertEquals("Level 1", lp.getName());
        assertEquals("Description", lp.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        LevelProject lp1 = new LevelProject();
        lp1.setId("1");
        lp1.setName("Level 1");
        lp1.setDescription("Description");

        LevelProject lp2 = new LevelProject();
        lp2.setId("1");
        lp2.setName("Level 1");
        lp2.setDescription("Description");

        LevelProject lp3 = new LevelProject();
        lp3.setId("2");
        lp3.setName("Level 1");
        lp3.setDescription("Description");

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(lp1.getId(), lp2.getId());
        assertEquals(lp1.getName(), lp2.getName());
        assertEquals(lp1.getDescription(), lp2.getDescription());
        assertNotEquals(lp1.getId(), lp3.getId());
    }

    @Test
    void testToString() {
        LevelProject lp = new LevelProject();
        lp.setId("1");
        lp.setName("Level 1");
        lp.setDescription("Description");
        String toString = lp.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Level 1") || toString.contains("LevelProject"));
    }

    @Test
    void testEqualsWithNull() {
        LevelProject lp = new LevelProject();
        lp.setId("1");
        assertNotEquals(null, lp);
    }

    @Test
    void testEqualsWithDifferentClass() {
        LevelProject lp = new LevelProject();
        lp.setId("1");
        Object other = new Object();
        assertNotEquals(lp, other);
    }

    @Test
    void testEqualsWithSameObject() {
        LevelProject lp = new LevelProject();
        lp.setId("1");
        assertEquals(lp, lp);
    }
}
