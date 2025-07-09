package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {
    @Test
    void testNoArgsConstructor() {
        Subject subject = new Subject();
        assertNull(subject.getCode());
        assertNull(subject.getName());
    }

    @Test
    void testAllArgsConstructor() {
        Subject subject = new Subject("SUBJ01", "Mathematics");
        assertEquals("SUBJ01", subject.getCode());
        assertEquals("Mathematics", subject.getName());
    }

    @Test
    void testSettersAndGetters() {
        Subject subject = new Subject();
        subject.setCode("SUBJ01");
        subject.setName("Mathematics");
        assertEquals("SUBJ01", subject.getCode());
        assertEquals("Mathematics", subject.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        Subject s1 = new Subject();
        s1.setId("1");
        s1.setCode("SUBJ01");
        s1.setName("Mathematics");

        Subject s2 = new Subject();
        s2.setId("1");
        s2.setCode("SUBJ01");
        s2.setName("Mathematics");

        Subject s3 = new Subject();
        s3.setId("2");
        s3.setCode("SUBJ01");
        s3.setName("Mathematics");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1.hashCode(), s3.hashCode());
    }

    @Test
    void testToString() {
        Subject subject = new Subject();
        subject.setId("1");
        subject.setCode("SUBJ01");
        subject.setName("Mathematics");
        String toString = subject.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Subject"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("code=SUBJ01"));
        assertTrue(toString.contains("name=Mathematics"));
    }

    @Test
    void testEqualsWithNull() {
        Subject subject = new Subject();
        subject.setId("1");
        assertNotEquals(null, subject);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Subject subject = new Subject();
        subject.setId("1");
        Object other = new Object();
        assertNotEquals(subject, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Subject subject = new Subject();
        subject.setId("1");
        assertEquals(subject, subject);
    }
}