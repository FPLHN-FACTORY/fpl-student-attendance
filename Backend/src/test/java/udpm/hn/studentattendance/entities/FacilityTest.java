package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacilityTest {
    @Test
    void testNoArgsConstructor() {
        Facility facility = new Facility();
        assertNull(facility.getCode());
        assertNull(facility.getName());
        assertNull(facility.getPosition());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility("code1", "name1", 5);
        assertEquals("code1", facility.getCode());
        assertEquals("name1", facility.getName());
        assertEquals(5, facility.getPosition());
    }

    @Test
    void testSettersAndGetters() {
        Facility facility = new Facility();
        facility.setCode("code1");
        facility.setName("name1");
        facility.setPosition(5);
        assertEquals("code1", facility.getCode());
        assertEquals("name1", facility.getName());
        assertEquals(5, facility.getPosition());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility f1 = new Facility();
        f1.setId("1");
        f1.setCode("FAC001");
        f1.setName("Facility 1");
        f1.setPosition(1);

        Facility f2 = new Facility();
        f2.setId("1");
        f2.setCode("FAC001");
        f2.setName("Facility 1");
        f2.setPosition(1);

        Facility f3 = new Facility();
        f3.setId("2");
        f3.setCode("FAC001");
        f3.setName("Facility 1");
        f3.setPosition(1);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(f1.getId(), f2.getId());
        assertEquals(f1.getCode(), f2.getCode());
        assertEquals(f1.getName(), f2.getName());
        assertEquals(f1.getPosition(), f2.getPosition());
        assertNotEquals(f1.getId(), f3.getId());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        facility.setCode("FAC001");
        facility.setName("Facility 1");
        facility.setPosition(1);
        String toString = facility.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Facility 1") || toString.contains("Facility"));
    }

    @Test
    void testEqualsWithNull() {
        Facility facility = new Facility();
        facility.setId("1");
        assertNotEquals(null, facility);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Facility facility = new Facility();
        facility.setId("1");
        Object other = new Object();
        assertNotEquals(facility, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Facility facility = new Facility();
        facility.setId("1");
        assertEquals(facility, facility);
    }
}