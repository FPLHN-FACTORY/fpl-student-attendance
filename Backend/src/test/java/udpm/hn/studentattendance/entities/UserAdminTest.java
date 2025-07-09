package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserAdminTest {

    @Test
    void testNoArgsConstructor() {
        UserAdmin userAdmin = new UserAdmin();
        assertNotNull(userAdmin);
        assertNull(userAdmin.getEmail());
        assertNull(userAdmin.getName());
        assertNull(userAdmin.getCode());
        assertNull(userAdmin.getImage());
    }

    @Test
    void testAllArgsConstructor() {
        UserAdmin userAdmin = new UserAdmin(
                "admin@email.com",
                "Admin Name",
                "ADM001",
                "img.png");

        assertEquals("admin@email.com", userAdmin.getEmail());
        assertEquals("Admin Name", userAdmin.getName());
        assertEquals("ADM001", userAdmin.getCode());
        assertEquals("img.png", userAdmin.getImage());
    }

    @Test
    void testSettersAndGetters() {
        UserAdmin userAdmin = new UserAdmin();

        userAdmin.setEmail("admin@email.com");
        userAdmin.setName("Admin Name");
        userAdmin.setCode("ADM001");
        userAdmin.setImage("img.png");

        assertEquals("admin@email.com", userAdmin.getEmail());
        assertEquals("Admin Name", userAdmin.getName());
        assertEquals("ADM001", userAdmin.getCode());
        assertEquals("img.png", userAdmin.getImage());
    }

    @Test
    void testEqualsAndHashCode() {
        UserAdmin userAdmin1 = new UserAdmin();
        userAdmin1.setId("1");
        userAdmin1.setEmail("admin@email.com");
        userAdmin1.setName("Admin Name");
        userAdmin1.setCode("ADM001");

        UserAdmin userAdmin2 = new UserAdmin();
        userAdmin2.setId("1");
        userAdmin2.setEmail("admin@email.com");
        userAdmin2.setName("Admin Name");
        userAdmin2.setCode("ADM001");

        UserAdmin userAdmin3 = new UserAdmin();
        userAdmin3.setId("2");
        userAdmin3.setEmail("admin@email.com");
        userAdmin3.setName("Admin Name");
        userAdmin3.setCode("ADM001");

        assertEquals(userAdmin1, userAdmin2);
        assertNotEquals(userAdmin1, userAdmin3);
        assertEquals(userAdmin1.hashCode(), userAdmin2.hashCode());
        assertNotEquals(userAdmin1.hashCode(), userAdmin3.hashCode());
    }

    @Test
    void testToString() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("1");
        userAdmin.setEmail("admin@email.com");
        userAdmin.setName("Admin Name");
        userAdmin.setCode("ADM001");

        String toString = userAdmin.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("UserAdmin"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("email=admin@email.com"));
        assertTrue(toString.contains("name=Admin Name"));
        assertTrue(toString.contains("code=ADM001"));
    }

    @Test
    void testEqualsWithNull() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("1");

        assertNotEquals(null, userAdmin);
    }

    @Test
    void testEqualsWithDifferentClass() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("1");

        Object other = new Object();
        assertNotEquals(userAdmin, other);
    }

    @Test
    void testEqualsWithSameObject() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("1");

        assertEquals(userAdmin, userAdmin);
    }
}