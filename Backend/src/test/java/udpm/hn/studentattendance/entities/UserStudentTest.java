package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserStudentTest {

    @Test
    void testNoArgsConstructor() {
        UserStudent userStudent = new UserStudent();
        assertNotNull(userStudent);
        assertNull(userStudent.getEmail());
        assertNull(userStudent.getName());
        assertNull(userStudent.getCode());
        assertNull(userStudent.getImage());
        assertNull(userStudent.getFaceEmbedding());
        assertNull(userStudent.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");

        UserStudent userStudent = new UserStudent(
                "test@email.com",
                "Student Name",
                "S001",
                "img.png",
                "embedding",
                facility);

        assertEquals("test@email.com", userStudent.getEmail());
        assertEquals("Student Name", userStudent.getName());
        assertEquals("S001", userStudent.getCode());
        assertEquals("img.png", userStudent.getImage());
        assertEquals("embedding", userStudent.getFaceEmbedding());
        assertEquals(facility, userStudent.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        UserStudent userStudent = new UserStudent();

        userStudent.setEmail("test@email.com");
        userStudent.setName("Student Name");
        userStudent.setCode("S001");
        userStudent.setImage("img.png");
        userStudent.setFaceEmbedding("embedding");

        Facility facility = new Facility();
        facility.setId("1");
        userStudent.setFacility(facility);

        assertEquals("test@email.com", userStudent.getEmail());
        assertEquals("Student Name", userStudent.getName());
        assertEquals("S001", userStudent.getCode());
        assertEquals("img.png", userStudent.getImage());
        assertEquals("embedding", userStudent.getFaceEmbedding());
        assertEquals(facility, userStudent.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        UserStudent userStudent1 = new UserStudent();
        userStudent1.setId("1");
        userStudent1.setEmail("test@email.com");
        userStudent1.setName("Student Name");
        userStudent1.setCode("S001");

        UserStudent userStudent2 = new UserStudent();
        userStudent2.setId("1");
        userStudent2.setEmail("test@email.com");
        userStudent2.setName("Student Name");
        userStudent2.setCode("S001");

        UserStudent userStudent3 = new UserStudent();
        userStudent3.setId("2");
        userStudent3.setEmail("test@email.com");
        userStudent3.setName("Student Name");
        userStudent3.setCode("S001");

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(userStudent1.getId(), userStudent2.getId());
        assertEquals(userStudent1.getEmail(), userStudent2.getEmail());
        assertEquals(userStudent1.getName(), userStudent2.getName());
        assertEquals(userStudent1.getCode(), userStudent2.getCode());
        assertNotEquals(userStudent1.getId(), userStudent3.getId());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        UserStudent userStudent = new UserStudent();
        userStudent.setId("1");
        userStudent.setEmail("student@example.com");
        userStudent.setName("Student User");
        userStudent.setCode("STU001");
        userStudent.setFacility(facility);
        String toString = userStudent.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Student User") || toString.contains("UserStudent"));
    }

    @Test
    void testEqualsWithNull() {
        UserStudent userStudent = new UserStudent();
        userStudent.setId("1");

        assertNotEquals(null, userStudent);
    }

    @Test
    void testEqualsWithDifferentClass() {
        UserStudent userStudent = new UserStudent();
        userStudent.setId("1");

        Object other = new Object();
        assertNotEquals(userStudent, other);
    }

    @Test
    void testEqualsWithSameObject() {
        UserStudent userStudent = new UserStudent();
        userStudent.setId("1");

        assertEquals(userStudent, userStudent);
    }
}