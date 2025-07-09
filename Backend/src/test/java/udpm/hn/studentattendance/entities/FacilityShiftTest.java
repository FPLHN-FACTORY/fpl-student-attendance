package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacilityShiftTest {
    @Test
    void testNoArgsConstructor() {
        FacilityShift shift = new FacilityShift();
        assertEquals(0, shift.getShift());
        assertEquals(0, shift.getFromHour());
        assertEquals(0, shift.getFromMinute());
        assertEquals(0, shift.getToHour());
        assertEquals(0, shift.getToMinute());
        assertNull(shift.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityShift shift = new FacilityShift(1, 8, 30, 10, 0, facility);
        assertEquals(1, shift.getShift());
        assertEquals(8, shift.getFromHour());
        assertEquals(30, shift.getFromMinute());
        assertEquals(10, shift.getToHour());
        assertEquals(0, shift.getToMinute());
        assertEquals(facility, shift.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        FacilityShift shift = new FacilityShift();
        Facility facility = new Facility();
        facility.setId("1");
        shift.setShift(1);
        shift.setFromHour(8);
        shift.setFromMinute(30);
        shift.setToHour(10);
        shift.setToMinute(0);
        shift.setFacility(facility);
        assertEquals(1, shift.getShift());
        assertEquals(8, shift.getFromHour());
        assertEquals(30, shift.getFromMinute());
        assertEquals(10, shift.getToHour());
        assertEquals(0, shift.getToMinute());
        assertEquals(facility, shift.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityShift shift1 = new FacilityShift();
        shift1.setId("1");
        shift1.setShift(1);
        shift1.setFromHour(8);
        shift1.setFromMinute(30);
        shift1.setToHour(10);
        shift1.setToMinute(0);
        shift1.setFacility(facility);

        FacilityShift shift2 = new FacilityShift();
        shift2.setId("1");
        shift2.setShift(1);
        shift2.setFromHour(8);
        shift2.setFromMinute(30);
        shift2.setToHour(10);
        shift2.setToMinute(0);
        shift2.setFacility(facility);

        FacilityShift shift3 = new FacilityShift();
        shift3.setId("2");
        shift3.setShift(1);
        shift3.setFromHour(8);
        shift3.setFromMinute(30);
        shift3.setToHour(10);
        shift3.setToMinute(0);
        shift3.setFacility(facility);

        assertEquals(shift1, shift2);
        assertNotEquals(shift1, shift3);
        assertEquals(shift1.hashCode(), shift2.hashCode());
        assertNotEquals(shift1.hashCode(), shift3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityShift shift = new FacilityShift();
        shift.setId("1");
        shift.setShift(1);
        shift.setFromHour(8);
        shift.setFromMinute(30);
        shift.setToHour(10);
        shift.setToMinute(0);
        shift.setFacility(facility);
        String toString = shift.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FacilityShift"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("shift=1"));
        assertTrue(toString.contains("fromHour=8"));
        assertTrue(toString.contains("fromMinute=30"));
        assertTrue(toString.contains("toHour=10"));
        assertTrue(toString.contains("toMinute=0"));
    }

    @Test
    void testEqualsWithNull() {
        FacilityShift shift = new FacilityShift();
        shift.setId("1");
        assertNotEquals(null, shift);
    }

    @Test
    void testEqualsWithDifferentClass() {
        FacilityShift shift = new FacilityShift();
        shift.setId("1");
        Object other = new Object();
        assertNotEquals(shift, other);
    }

    @Test
    void testEqualsWithSameObject() {
        FacilityShift shift = new FacilityShift();
        shift.setId("1");
        assertEquals(shift, shift);
    }
}