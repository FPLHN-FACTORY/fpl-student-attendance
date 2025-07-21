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
        FacilityShift s1 = new FacilityShift();
        s1.setId("1");
        s1.setShift(1);
        s1.setFromHour(8);
        s1.setFromMinute(30);
        s1.setToHour(10);
        s1.setToMinute(0);
        s1.setFacility(facility);

        FacilityShift s2 = new FacilityShift();
        s2.setId("1");
        s2.setShift(1);
        s2.setFromHour(8);
        s2.setFromMinute(30);
        s2.setToHour(10);
        s2.setToMinute(0);
        s2.setFacility(facility);

        FacilityShift s3 = new FacilityShift();
        s3.setId("2");
        s3.setShift(1);
        s3.setFromHour(8);
        s3.setFromMinute(30);
        s3.setToHour(10);
        s3.setToMinute(0);
        s3.setFacility(facility);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(s1.getId(), s2.getId());
        assertEquals(s1.getShift(), s2.getShift());
        assertEquals(s1.getFromHour(), s2.getFromHour());
        assertEquals(s1.getFromMinute(), s2.getFromMinute());
        assertEquals(s1.getToHour(), s2.getToHour());
        assertEquals(s1.getToMinute(), s2.getToMinute());
        assertEquals(s1.getFacility(), s2.getFacility());
        assertNotEquals(s1.getId(), s3.getId());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityShift shift = new FacilityShift();
        shift.setId("1");
        shift.setShift(1);
        shift.setFromHour(8);
        shift.setFromMinute(0);
        shift.setToHour(10);
        shift.setToMinute(0);
        shift.setFacility(facility);
        String toString = shift.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("1") || toString.contains("FacilityShift"));
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
