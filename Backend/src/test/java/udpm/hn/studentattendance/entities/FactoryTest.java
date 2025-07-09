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
        Project project = new Project();
        project.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        Factory f1 = new Factory();
        f1.setId("1");
        f1.setName("Factory A");
        f1.setDescription("Description");
        f1.setProject(project);
        f1.setUserStaff(userStaff);

        Factory f2 = new Factory();
        f2.setId("1");
        f2.setName("Factory A");
        f2.setDescription("Description");
        f2.setProject(project);
        f2.setUserStaff(userStaff);

        Factory f3 = new Factory();
        f3.setId("2");
        f3.setName("Factory A");
        f3.setDescription("Description");
        f3.setProject(project);
        f3.setUserStaff(userStaff);

        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1.hashCode(), f3.hashCode());
    }

    @Test
    void testToString() {
        Project project = new Project();
        project.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        Factory factory = new Factory();
        factory.setId("1");
        factory.setName("Factory A");
        factory.setDescription("Description");
        factory.setProject(project);
        factory.setUserStaff(userStaff);
        String toString = factory.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Factory"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=Factory A"));
        assertTrue(toString.contains("description=Description"));
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