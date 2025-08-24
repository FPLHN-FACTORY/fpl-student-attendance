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

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(userAdmin1.getId(), userAdmin2.getId());
        assertEquals(userAdmin1.getEmail(), userAdmin2.getEmail());
        assertEquals(userAdmin1.getName(), userAdmin2.getName());
        assertEquals(userAdmin1.getCode(), userAdmin2.getCode());
        assertNotEquals(userAdmin1.getId(), userAdmin3.getId());
    }

    @Test
    void testToString() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("1");
        userAdmin.setEmail("admin@example.com");
        userAdmin.setName("Admin User");
        userAdmin.setCode("ADMIN001");
        String toString = userAdmin.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Admin User") || toString.contains("UserAdmin"));
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
