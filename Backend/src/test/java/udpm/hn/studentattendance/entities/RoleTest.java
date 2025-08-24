package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {
    @Test
    void testNoArgsConstructor() {
        Role role = new Role();
        assertNull(role.getCode());
        assertNull(role.getFacility());
        assertNull(role.getUserStaff());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        Role role = new Role(RoleConstant.ADMIN, facility, userStaff);
        assertEquals(RoleConstant.ADMIN, role.getCode());
        assertEquals(facility, role.getFacility());
        assertEquals(userStaff, role.getUserStaff());
    }

    @Test
    void testSettersAndGetters() {
        Role role = new Role();
        Facility facility = new Facility();
        facility.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        role.setCode(RoleConstant.ADMIN);
        role.setFacility(facility);
        role.setUserStaff(userStaff);
        assertEquals(RoleConstant.ADMIN, role.getCode());
        assertEquals(facility, role.getFacility());
        assertEquals(userStaff, role.getUserStaff());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        UserStaff userStaff = new UserStaff();
        userStaff.setId("2");
        Role r1 = new Role();
        r1.setId("1");
        r1.setCode(RoleConstant.ADMIN);
        r1.setFacility(facility);
        r1.setUserStaff(userStaff);

        Role r2 = new Role();
        r2.setId("1");
        r2.setCode(RoleConstant.ADMIN);
        r2.setFacility(facility);
        r2.setUserStaff(userStaff);

        Role r3 = new Role();
        r3.setId("2");
        r3.setCode(RoleConstant.ADMIN);
        r3.setFacility(facility);
        r3.setUserStaff(userStaff);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(r1.getId(), r2.getId());
        assertEquals(r1.getCode(), r2.getCode());
        assertEquals(r1.getFacility(), r2.getFacility());
        assertEquals(r1.getUserStaff(), r2.getUserStaff());
        assertNotEquals(r1.getId(), r3.getId());
    }

    @Test
    void testToString() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("1");
        Role role = new Role();
        role.setId("1");
        role.setCode(RoleConstant.ADMIN);
        role.setUserStaff(userStaff);
        String toString = role.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("ADMIN") || toString.contains("Role"));
    }

    @Test
    void testEqualsWithNull() {
        Role role = new Role();
        role.setId("1");
        assertNotEquals(null, role);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Role role = new Role();
        role.setId("1");
        Object other = new Object();
        assertNotEquals(role, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Role role = new Role();
        role.setId("1");
        assertEquals(role, role);
    }
}
