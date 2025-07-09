package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserStudentFactoryTest {
    @Test
    void testNoArgsConstructor() {
        UserStudentFactory usf = new UserStudentFactory();
        assertNull(usf.getFactory());
        assertNull(usf.getUserStudent());
    }

    @Test
    void testAllArgsConstructor() {
        Factory factory = new Factory();
        factory.setId("1");
        UserStudent userStudent = new UserStudent();
        userStudent.setId("2");
        UserStudentFactory usf = new UserStudentFactory(factory, userStudent);
        assertEquals(factory, usf.getFactory());
        assertEquals(userStudent, usf.getUserStudent());
    }

    @Test
    void testSettersAndGetters() {
        UserStudentFactory usf = new UserStudentFactory();
        Factory factory = new Factory();
        factory.setId("1");
        UserStudent userStudent = new UserStudent();
        userStudent.setId("2");
        usf.setFactory(factory);
        usf.setUserStudent(userStudent);
        assertEquals(factory, usf.getFactory());
        assertEquals(userStudent, usf.getUserStudent());
    }

    @Test
    void testEqualsAndHashCode() {
        UserStudentFactory usf1 = new UserStudentFactory();
        usf1.setId("1");
        Factory factory = new Factory();
        factory.setId("1");
        UserStudent userStudent = new UserStudent();
        userStudent.setId("2");
        usf1.setFactory(factory);
        usf1.setUserStudent(userStudent);

        UserStudentFactory usf2 = new UserStudentFactory();
        usf2.setId("1");
        usf2.setFactory(factory);
        usf2.setUserStudent(userStudent);

        UserStudentFactory usf3 = new UserStudentFactory();
        usf3.setId("2");
        usf3.setFactory(factory);
        usf3.setUserStudent(userStudent);

        assertEquals(usf1, usf2);
        assertNotEquals(usf1, usf3);
        assertEquals(usf1.hashCode(), usf2.hashCode());
        assertNotEquals(usf1.hashCode(), usf3.hashCode());
    }

    @Test
    void testToString() {
        UserStudentFactory usf = new UserStudentFactory();
        usf.setId("1");
        Factory factory = new Factory();
        factory.setId("1");
        usf.setFactory(factory);
        UserStudent userStudent = new UserStudent();
        userStudent.setId("2");
        usf.setUserStudent(userStudent);
        String toString = usf.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("UserStudentFactory"));
        assertTrue(toString.contains("id=1"));
    }
    
    @Test
    void testEqualsWithNull() {
        UserStudentFactory usf = new UserStudentFactory();
        usf.setId("1");
        assertNotEquals(null, usf);
    }
    
    @Test
    void testEqualsWithDifferentClass() {
        UserStudentFactory usf = new UserStudentFactory();
        usf.setId("1");
        Object other = new Object();
        assertNotEquals(usf, other);
    }
    
    @Test
    void testEqualsWithSameObject() {
        UserStudentFactory usf = new UserStudentFactory();
        usf.setId("1");
        assertEquals(usf, usf);
    }
}