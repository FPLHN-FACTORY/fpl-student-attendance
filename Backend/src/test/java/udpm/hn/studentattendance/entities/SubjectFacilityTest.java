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

        assertEquals(sf1, sf2);
        assertNotEquals(sf1, sf3);
        assertEquals(sf1.hashCode(), sf2.hashCode());
        assertNotEquals(sf1.hashCode(), sf3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        Subject subject = new Subject();
        subject.setId("2");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        String toString = subjectFacility.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SubjectFacility"));
        assertTrue(toString.contains("id=1"));
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