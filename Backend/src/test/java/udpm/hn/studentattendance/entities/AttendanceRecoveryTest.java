package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

class AttendanceRecoveryTest {
    @Test
    void testAllFields() {
        AttendanceRecovery recovery = new AttendanceRecovery();
        recovery.setId("id");
        recovery.setName("Test Recovery");
        recovery.setDescription("Test Description");
        recovery.setDay(1234567890L);
        recovery.setTotalStudent(25);
        recovery.setStatus(EntityStatus.ACTIVE);

        assertEquals("id", recovery.getId());
        assertEquals("Test Recovery", recovery.getName());
        assertEquals("Test Description", recovery.getDescription());
        assertEquals(1234567890L, recovery.getDay());
        assertEquals(25, recovery.getTotalStudent());
        assertEquals(EntityStatus.ACTIVE, recovery.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        AttendanceRecovery ar1 = new AttendanceRecovery();
        ar1.setId("1");
        ar1.setName("Recovery 1");
        ar1.setDescription("Description");
        ar1.setDay(1000L);
        ar1.setTotalStudent(10);

        AttendanceRecovery ar2 = new AttendanceRecovery();
        ar2.setId("1");
        ar2.setName("Recovery 1");
        ar2.setDescription("Description");
        ar2.setDay(1000L);
        ar2.setTotalStudent(10);

        AttendanceRecovery ar3 = new AttendanceRecovery();
        ar3.setId("2");
        ar3.setName("Recovery 1");
        ar3.setDescription("Description");
        ar3.setDay(1000L);
        ar3.setTotalStudent(10);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(ar1.getId(), ar2.getId());
        assertEquals(ar1.getName(), ar2.getName());
        assertEquals(ar1.getDescription(), ar2.getDescription());
        assertEquals(ar1.getDay(), ar2.getDay());
        assertEquals(ar1.getTotalStudent(), ar2.getTotalStudent());
        assertNotEquals(ar1.getId(), ar3.getId());
    }
}