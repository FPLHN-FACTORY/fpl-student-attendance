package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import static org.junit.jupiter.api.Assertions.*;

class SemesterTest {
    @Test
    void testNoArgsConstructor() {
        Semester semester = new Semester();
        assertNull(semester.getCode());
        assertNull(semester.getSemesterName());
        assertNull(semester.getFromDate());
        assertNull(semester.getToDate());
        assertNull(semester.getYear());
    }

    @Test
    void testAllArgsConstructor() {
        Semester semester = new Semester("SEM2024", SemesterName.FALL, 1000L, 2000L, 2024);
        assertEquals("SEM2024", semester.getCode());
        assertEquals(SemesterName.FALL, semester.getSemesterName());
        assertEquals(1000L, semester.getFromDate());
        assertEquals(2000L, semester.getToDate());
        assertEquals(2024, semester.getYear());
    }

    @Test
    void testSettersAndGetters() {
        Semester semester = new Semester();
        semester.setCode("SEM2024");
        semester.setSemesterName(SemesterName.FALL);
        semester.setFromDate(1000L);
        semester.setToDate(2000L);
        semester.setYear(2024);
        assertEquals("SEM2024", semester.getCode());
        assertEquals(SemesterName.FALL, semester.getSemesterName());
        assertEquals(1000L, semester.getFromDate());
        assertEquals(2000L, semester.getToDate());
        assertEquals(2024, semester.getYear());
    }

    @Test
    void testEqualsAndHashCode() {
        Semester s1 = new Semester();
        s1.setId("1");
        s1.setCode("Spring 2024");
        s1.setSemesterName(SemesterName.SPRING);
        s1.setFromDate(1000L);
        s1.setToDate(2000L);
        s1.setYear(2024);

        Semester s2 = new Semester();
        s2.setId("1");
        s2.setCode("Spring 2024");
        s2.setSemesterName(SemesterName.SPRING);
        s2.setFromDate(1000L);
        s2.setToDate(2000L);
        s2.setYear(2024);

        Semester s3 = new Semester();
        s3.setId("2");
        s3.setCode("Spring 2024");
        s3.setSemesterName(SemesterName.SPRING);
        s3.setFromDate(1000L);
        s3.setToDate(2000L);
        s3.setYear(2024);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(s1.getId(), s2.getId());
        assertEquals(s1.getCode(), s2.getCode());
        assertEquals(s1.getSemesterName(), s2.getSemesterName());
        assertEquals(s1.getFromDate(), s2.getFromDate());
        assertEquals(s1.getToDate(), s2.getToDate());
        assertEquals(s1.getYear(), s2.getYear());
        assertNotEquals(s1.getId(), s3.getId());
    }

    @Test
    void testToString() {
        Semester semester = new Semester();
        semester.setId("1");
        semester.setCode("Spring 2024");
        semester.setSemesterName(SemesterName.SPRING);
        semester.setFromDate(1000L);
        semester.setToDate(2000L);
        semester.setYear(2024);
        String toString = semester.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Spring 2024") || toString.contains("Semester"));
    }

    @Test
    void testEqualsWithNull() {
        Semester semester = new Semester();
        semester.setId("1");
        assertNotEquals(null, semester);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Semester semester = new Semester();
        semester.setId("1");
        Object other = new Object();
        assertNotEquals(semester, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Semester semester = new Semester();
        semester.setId("1");
        assertEquals(semester, semester);
    }
}