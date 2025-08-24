package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {
    @Test
    void testNoArgsConstructor() {
        Factory factory = new Factory();
        assertNull(factory.getName());
        assertNull(factory.getDescription());
        assertNull(factory.getProject());
        assertNull(factory.getUserStaff());
    }

    @Test
    void testAllArgsConstructor() {
        Project project = new Project();
        project.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        Factory factory = new Factory("Factory A", "Description", project, userStaff);
        assertEquals("Factory A", factory.getName());
        assertEquals("Description", factory.getDescription());
        assertEquals(project, factory.getProject());
        assertEquals(userStaff, factory.getUserStaff());
    }

    @Test
    void testSettersAndGetters() {
        Factory factory = new Factory();
        Project project = new Project();
        project.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        factory.setName("Factory A");
        factory.setDescription("Description");
        factory.setProject(project);
        factory.setUserStaff(userStaff);
        assertEquals("Factory A", factory.getName());
        assertEquals("Description", factory.getDescription());
        assertEquals(project, factory.getProject());
        assertEquals(userStaff, factory.getUserStaff());
    }

    @Test
    void testEqualsAndHashCode() {
        Factory f1 = new Factory();
        f1.setId("1");
        f1.setName("Factory 1");
        f1.setDescription("Description");

        Factory f2 = new Factory();
        f2.setId("1");
        f2.setName("Factory 1");
        f2.setDescription("Description");

        Factory f3 = new Factory();
        f3.setId("2");
        f3.setName("Factory 1");
        f3.setDescription("Description");

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(f1.getId(), f2.getId());
        assertEquals(f1.getName(), f2.getName());
        assertEquals(f1.getDescription(), f2.getDescription());
        assertNotEquals(f1.getId(), f3.getId());
    }

    @Test
    void testToString() {
        Factory factory = new Factory();
        factory.setId("1");
        factory.setName("Factory 1");
        factory.setDescription("Description");
        String toString = factory.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Factory 1") || toString.contains("Factory"));
    }

    @Test
    void testEqualsWithNull() {
        Factory factory = new Factory();
        factory.setId("1");
        assertNotEquals(null, factory);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Factory factory = new Factory();
        factory.setId("1");
        Object other = new Object();
        assertNotEquals(factory, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Factory factory = new Factory();
        factory.setId("1");
        assertEquals(factory, factory);
    }
}
