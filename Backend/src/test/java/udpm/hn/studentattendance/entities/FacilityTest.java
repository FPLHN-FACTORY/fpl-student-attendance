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
        f1.setCode("code1");
        f1.setName("name1");
        f1.setPosition(5);

        Facility f2 = new Facility();
        f2.setId("1");
        f2.setCode("code1");
        f2.setName("name1");
        f2.setPosition(5);

        Facility f3 = new Facility();
        f3.setId("2");
        f3.setCode("code1");
        f3.setName("name1");
        f3.setPosition(5);

        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1.hashCode(), f3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        facility.setCode("code1");
        facility.setName("name1");
        facility.setPosition(5);
        String toString = facility.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Facility"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("code=code1"));
        assertTrue(toString.contains("name=name1"));
        assertTrue(toString.contains("position=5"));
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