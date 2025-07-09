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
}