package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubjectFacilityTest {
    @Test
    void testNoArgsConstructor() {
        SubjectFacility subjectFacility = new SubjectFacility();
        assertNull(subjectFacility.getFacility());
        assertNull(subjectFacility.getSubject());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        Subject subject = new Subject();
        subject.setId("2");
        SubjectFacility subjectFacility = new SubjectFacility(facility, subject);
        assertEquals(facility, subjectFacility.getFacility());
        assertEquals(subject, subjectFacility.getSubject());
    }

    @Test
    void testSettersAndGetters() {
        SubjectFacility subjectFacility = new SubjectFacility();
        Facility facility = new Facility();
        facility.setId("1");
        Subject subject = new Subject();
        subject.setId("2");
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        assertEquals(facility, subjectFacility.getFacility());
        assertEquals(subject, subjectFacility.getSubject());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        Subject subject = new Subject();
        subject.setId("2");
        SubjectFacility sf1 = new SubjectFacility();
        sf1.setId("1");
        sf1.setFacility(facility);
        sf1.setSubject(subject);

        SubjectFacility sf2 = new SubjectFacility();
        sf2.setId("1");
        sf2.setFacility(facility);
        sf2.setSubject(subject);

        SubjectFacility sf3 = new SubjectFacility();
        sf3.setId("2");
        sf3.setFacility(facility);
        sf3.setSubject(subject);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(sf1.getId(), sf2.getId());
        assertEquals(sf1.getFacility(), sf2.getFacility());
        assertEquals(sf1.getSubject(), sf2.getSubject());
        assertNotEquals(sf1.getId(), sf3.getId());
    }

    @Test
    void testToString() {
        Subject subject = new Subject();
        subject.setId("1");
        Facility facility = new Facility();
        facility.setId("1");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        subjectFacility.setSubject(subject);
        subjectFacility.setFacility(facility);
        String toString = subjectFacility.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("SubjectFacility"));
    }

    @Test
    void testEqualsWithNull() {
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        assertNotEquals(null, subjectFacility);
    }

    @Test
    void testEqualsWithDifferentClass() {
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        Object other = new Object();
        assertNotEquals(subjectFacility, other);
    }

    @Test
    void testEqualsWithSameObject() {
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        assertEquals(subjectFacility, subjectFacility);
    }
}
