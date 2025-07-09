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
        s1.setCode("SEM2024");
        s1.setSemesterName(SemesterName.FALL);
        s1.setFromDate(1000L);
        s1.setToDate(2000L);
        s1.setYear(2024);

        Semester s2 = new Semester();
        s2.setId("1");
        s2.setCode("SEM2024");
        s2.setSemesterName(SemesterName.FALL);
        s2.setFromDate(1000L);
        s2.setToDate(2000L);
        s2.setYear(2024);

        Semester s3 = new Semester();
        s3.setId("2");
        s3.setCode("SEM2024");
        s3.setSemesterName(SemesterName.FALL);
        s3.setFromDate(1000L);
        s3.setToDate(2000L);
        s3.setYear(2024);

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1.hashCode(), s3.hashCode());
    }

    @Test
    void testToString() {
        Semester semester = new Semester();
        semester.setId("1");
        semester.setCode("SEM2024");
        semester.setSemesterName(SemesterName.FALL);
        semester.setFromDate(1000L);
        semester.setToDate(2000L);
        semester.setYear(2024);
        String toString = semester.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Semester"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("code=SEM2024"));
        assertTrue(toString.contains("semesterName=FALL"));
        assertTrue(toString.contains("fromDate=1000"));
        assertTrue(toString.contains("toDate=2000"));
        assertTrue(toString.contains("year=2024"));
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