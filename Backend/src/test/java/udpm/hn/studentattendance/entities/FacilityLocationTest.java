package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacilityLocationTest {
    @Test
    void testNoArgsConstructor() {
        FacilityLocation location = new FacilityLocation();
        assertNull(location.getName());
        assertNull(location.getLatitude());
        assertNull(location.getLongitude());
        assertNull(location.getRadius());
        assertNull(location.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityLocation location = new FacilityLocation("Main Gate", 10.123, 20.456, 50, facility);
        assertEquals("Main Gate", location.getName());
        assertEquals(10.123, location.getLatitude());
        assertEquals(20.456, location.getLongitude());
        assertEquals(50, location.getRadius());
        assertEquals(facility, location.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        FacilityLocation location = new FacilityLocation();
        Facility facility = new Facility();
        facility.setId("1");
        location.setName("Main Gate");
        location.setLatitude(10.123);
        location.setLongitude(20.456);
        location.setRadius(50);
        location.setFacility(facility);
        assertEquals("Main Gate", location.getName());
        assertEquals(10.123, location.getLatitude());
        assertEquals(20.456, location.getLongitude());
        assertEquals(50, location.getRadius());
        assertEquals(facility, location.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityLocation loc1 = new FacilityLocation();
        loc1.setId("1");
        loc1.setName("Main Gate");
        loc1.setLatitude(10.123);
        loc1.setLongitude(20.456);
        loc1.setRadius(50);
        loc1.setFacility(facility);

        FacilityLocation loc2 = new FacilityLocation();
        loc2.setId("1");
        loc2.setName("Main Gate");
        loc2.setLatitude(10.123);
        loc2.setLongitude(20.456);
        loc2.setRadius(50);
        loc2.setFacility(facility);

        FacilityLocation loc3 = new FacilityLocation();
        loc3.setId("2");
        loc3.setName("Main Gate");
        loc3.setLatitude(10.123);
        loc3.setLongitude(20.456);
        loc3.setRadius(50);
        loc3.setFacility(facility);

        assertEquals(loc1, loc2);
        assertNotEquals(loc1, loc3);
        assertEquals(loc1.hashCode(), loc2.hashCode());
        assertNotEquals(loc1.hashCode(), loc3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityLocation location = new FacilityLocation();
        location.setId("1");
        location.setName("Main Gate");
        location.setLatitude(10.123);
        location.setLongitude(20.456);
        location.setRadius(50);
        location.setFacility(facility);
        String toString = location.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FacilityLocation"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=Main Gate"));
        assertTrue(toString.contains("latitude=10.123"));
        assertTrue(toString.contains("longitude=20.456"));
        assertTrue(toString.contains("radius=50"));
    }

    @Test
    void testEqualsWithNull() {
        FacilityLocation location = new FacilityLocation();
        location.setId("1");
        assertNotEquals(null, location);
    }

    @Test
    void testEqualsWithDifferentClass() {
        FacilityLocation location = new FacilityLocation();
        location.setId("1");
        Object other = new Object();
        assertNotEquals(location, other);
    }

    @Test
    void testEqualsWithSameObject() {
        FacilityLocation location = new FacilityLocation();
        location.setId("1");
        assertEquals(location, location);
    }
}