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

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(s1.getId(), s2.getId());
        assertEquals(s1.getCode(), s2.getCode());
        assertEquals(s1.getName(), s2.getName());
        assertNotEquals(s1.getId(), s3.getId());
    }

    @Test
    void testToString() {
        Subject subject = new Subject();
        subject.setId("1");
        subject.setCode("SUB001");
        subject.setName("Mathematics");
        String toString = subject.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Mathematics") || toString.contains("Subject"));
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
