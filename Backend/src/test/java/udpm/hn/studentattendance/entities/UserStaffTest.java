package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

class UserStaffTest {

    @Test
    void testNoArgsConstructor() {
        UserStaff userStaff = new UserStaff();
        assertNotNull(userStaff);
        assertNull(userStaff.getEmailFe());
        assertNull(userStaff.getEmailFpt());
        assertNull(userStaff.getName());
        assertNull(userStaff.getCode());
        assertNull(userStaff.getImage());
        assertNull(userStaff.getRoles());
    }

    @Test
    void testAllArgsConstructor() {
        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setId("1");
        role.setCode(RoleConstant.STAFF);
        roles.add(role);

        UserStaff userStaff = new UserStaff(
                "fe@email.com",
                "fpt@email.com",
                "Staff Name",
                "STF001",
                "img.png",
                roles);

        assertEquals("fe@email.com", userStaff.getEmailFe());
        assertEquals("fpt@email.com", userStaff.getEmailFpt());
        assertEquals("Staff Name", userStaff.getName());
        assertEquals("STF001", userStaff.getCode());
        assertEquals("img.png", userStaff.getImage());
        assertEquals(roles, userStaff.getRoles());
    }

    @Test
    void testSettersAndGetters() {
        UserStaff userStaff = new UserStaff();

        userStaff.setEmailFe("fe@email.com");
        userStaff.setEmailFpt("fpt@email.com");
        userStaff.setName("Staff Name");
        userStaff.setCode("STF001");
        userStaff.setImage("img.png");

        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setId("1");
        role.setCode(RoleConstant.STAFF);
        roles.add(role);
        userStaff.setRoles(roles);

        assertEquals("fe@email.com", userStaff.getEmailFe());
        assertEquals("fpt@email.com", userStaff.getEmailFpt());
        assertEquals("Staff Name", userStaff.getName());
        assertEquals("STF001", userStaff.getCode());
        assertEquals("img.png", userStaff.getImage());
        assertEquals(roles, userStaff.getRoles());
    }

    @Test
    void testEqualsAndHashCode() {
        UserStaff userStaff1 = new UserStaff();
        userStaff1.setId("1");
        userStaff1.setEmailFe("fe@email.com");
        userStaff1.setName("Staff Name");
        userStaff1.setCode("STF001");

        UserStaff userStaff2 = new UserStaff();
        userStaff2.setId("1");
        userStaff2.setEmailFe("fe@email.com");
        userStaff2.setName("Staff Name");
        userStaff2.setCode("STF001");

        UserStaff userStaff3 = new UserStaff();
        userStaff3.setId("2");
        userStaff3.setEmailFe("fe@email.com");
        userStaff3.setName("Staff Name");
        userStaff3.setCode("STF001");

        assertEquals(userStaff1, userStaff2);
        assertNotEquals(userStaff1, userStaff3);
        assertEquals(userStaff1.hashCode(), userStaff2.hashCode());
        assertNotEquals(userStaff1.hashCode(), userStaff3.hashCode());
    }

    @Test
    void testToString() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("1");
        userStaff.setEmailFe("fe@email.com");
        userStaff.setName("Staff Name");
        userStaff.setCode("STF001");

        String toString = userStaff.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("UserStaff"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("emailFe=fe@email.com"));
        assertTrue(toString.contains("name=Staff Name"));
        assertTrue(toString.contains("code=STF001"));
    }

    @Test
    void testEqualsWithNull() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("1");

        assertNotEquals(null, userStaff);
    }

    @Test
    void testEqualsWithDifferentClass() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("1");

        Object other = new Object();
        assertNotEquals(userStaff, other);
    }

    @Test
    void testEqualsWithSameObject() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("1");

        assertEquals(userStaff, userStaff);
    }

    @Test
    void testGetAuthorities() {
        UserStaff userStaff = new UserStaff();
        List<Role> roles = new ArrayList<>();

        Role role1 = new Role();
        role1.setId("1");
        role1.setCode(RoleConstant.STAFF);
        roles.add(role1);

        Role role2 = new Role();
        role2.setId("2");
        role2.setCode(RoleConstant.ADMIN);
        roles.add(role2);

        userStaff.setRoles(roles);

        var authorities = userStaff.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
    }

    @Test
    void testGetAuthoritiesWithNullRoles() {
        UserStaff userStaff = new UserStaff();
        userStaff.setRoles(null);

        var authorities = userStaff.getAuthorities();
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void testGetPassword() {
        UserStaff userStaff = new UserStaff();
        assertNull(userStaff.getPassword());
    }

    @Test
    void testGetUsername() {
        UserStaff userStaff = new UserStaff();
        userStaff.setCode("STF001");
        assertEquals("STF001", userStaff.getUsername());
    }

    @Test
    void testGetEmailWithEmailFe() {
        UserStaff userStaff = new UserStaff();
        userStaff.setEmailFe("fe@email.com");
        userStaff.setEmailFpt("fpt@email.com");

        assertEquals("fe@email.com", userStaff.getEmail());
    }

    @Test
    void testGetEmailWithEmptyEmailFe() {
        UserStaff userStaff = new UserStaff();
        userStaff.setEmailFe("");
        userStaff.setEmailFpt("fpt@email.com");

        assertEquals("fpt@email.com", userStaff.getEmail());
    }

    @Test
    void testGetEmailWithNullEmailFe() {
        UserStaff userStaff = new UserStaff();
        userStaff.setEmailFe(null);
        userStaff.setEmailFpt("fpt@email.com");

        assertEquals("fpt@email.com", userStaff.getEmail());
    }

    @Test
    void testGetEmailWithWhitespaceEmailFe() {
        UserStaff userStaff = new UserStaff();
        userStaff.setEmailFe("   ");
        userStaff.setEmailFpt("fpt@email.com");

        assertEquals("fpt@email.com", userStaff.getEmail());
    }

    @Test
    void testGetEmailWithNullBothEmails() {
        UserStaff userStaff = new UserStaff();
        userStaff.setEmailFe(null);
        userStaff.setEmailFpt(null);

        assertNull(userStaff.getEmail());
    }
}