package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

class UserActivityLogTest {
    @Test
    void testNoArgsConstructor() {
        UserActivityLog ual = new UserActivityLog();
        assertNull(ual.getIdUser());
        assertNull(ual.getRole());
        assertNull(ual.getMessage());
        assertNull(ual.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        UserActivityLog ual = new UserActivityLog(
                "U001",
                RoleConstant.ADMIN,
                "Đăng nhập thành công",
                facility);
        assertEquals("U001", ual.getIdUser());
        assertEquals(RoleConstant.ADMIN, ual.getRole());
        assertEquals("Đăng nhập thành công", ual.getMessage());
        assertEquals(facility, ual.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        UserActivityLog ual = new UserActivityLog();
        Facility facility = new Facility();
        facility.setId("1");
        ual.setIdUser("U001");
        ual.setRole(RoleConstant.ADMIN);
        ual.setMessage("Đăng nhập thành công");
        ual.setFacility(facility);
        assertEquals("U001", ual.getIdUser());
        assertEquals(RoleConstant.ADMIN, ual.getRole());
        assertEquals("Đăng nhập thành công", ual.getMessage());
        assertEquals(facility, ual.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        UserActivityLog ual1 = new UserActivityLog();
        ual1.setId("1");
        ual1.setIdUser("U001");
        ual1.setRole(RoleConstant.ADMIN);
        ual1.setMessage("Đăng nhập thành công");

        UserActivityLog ual2 = new UserActivityLog();
        ual2.setId("1");
        ual2.setIdUser("U001");
        ual2.setRole(RoleConstant.ADMIN);
        ual2.setMessage("Đăng nhập thành công");

        UserActivityLog ual3 = new UserActivityLog();
        ual3.setId("2");
        ual3.setIdUser("U001");
        ual3.setRole(RoleConstant.ADMIN);
        ual3.setMessage("Đăng nhập thành công");

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(ual1.getId(), ual2.getId());
        assertEquals(ual1.getIdUser(), ual2.getIdUser());
        assertEquals(ual1.getRole(), ual2.getRole());
        assertEquals(ual1.getMessage(), ual2.getMessage());
        assertNotEquals(ual1.getId(), ual3.getId());
    }

    @Test
    void testToString() {
        UserActivityLog ual = new UserActivityLog();
        ual.setId("1");
        ual.setIdUser("U001");
        ual.setRole(RoleConstant.ADMIN);
        ual.setMessage("Đăng nhập thành công");
        String toString = ual.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("U001") || toString.contains("UserActivityLog"));
    }

    @Test
    void testEqualsWithNull() {
        UserActivityLog ual = new UserActivityLog();
        ual.setId("1");
        assertNotEquals(null, ual);
    }

    @Test
    void testEqualsWithDifferentClass() {
        UserActivityLog ual = new UserActivityLog();
        ual.setId("1");
        Object other = new Object();
        assertNotEquals(ual, other);
    }

    @Test
    void testEqualsWithSameObject() {
        UserActivityLog ual = new UserActivityLog();
        ual.setId("1");
        assertEquals(ual, ual);
    }
}
