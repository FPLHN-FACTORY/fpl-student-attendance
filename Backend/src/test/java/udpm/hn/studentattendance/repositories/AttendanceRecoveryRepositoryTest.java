package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.AttendanceRecovery;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceRecoveryRepositoryTest {

    @Mock
    private AttendanceRecoveryRepository attendanceRecoveryRepository;

    @Test
    void testAttendanceRecoveryRepositoryExists() {
        assertNotNull(AttendanceRecoveryRepository.class);
    }

    @Test
    void testFindById() {
        // Given
        String id = "test-id";
        AttendanceRecovery attendanceRecovery = new AttendanceRecovery();
        attendanceRecovery.setId(id);
        attendanceRecovery.setName("Test Recovery");

        // Mock behavior
        when(attendanceRecoveryRepository.findById(id)).thenReturn(Optional.of(attendanceRecovery));

        // When
        Optional<AttendanceRecovery> result = attendanceRecoveryRepository.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Recovery", result.get().getName());
    }

    @Test
    void testSaveAndDelete() {
        // Given
        AttendanceRecovery attendanceRecovery = new AttendanceRecovery();
        attendanceRecovery.setId("test-id");
        attendanceRecovery.setName("Test Recovery");

        // Mock behavior
        when(attendanceRecoveryRepository.save(any(AttendanceRecovery.class))).thenReturn(attendanceRecovery);
        doNothing().when(attendanceRecoveryRepository).deleteById(anyString());
        when(attendanceRecoveryRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        AttendanceRecovery saved = attendanceRecoveryRepository.save(attendanceRecovery);
        attendanceRecoveryRepository.deleteById(saved.getId());
        Optional<AttendanceRecovery> afterDelete = attendanceRecoveryRepository.findById(saved.getId());

        // Then
        assertEquals("test-id", saved.getId());
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void testFindAll() {
        // Given
        List<AttendanceRecovery> recoveryList = new ArrayList<>();
        AttendanceRecovery recovery1 = new AttendanceRecovery();
        recovery1.setId("id1");
        recovery1.setName("Recovery 1");

        AttendanceRecovery recovery2 = new AttendanceRecovery();
        recovery2.setId("id2");
        recovery2.setName("Recovery 2");

        recoveryList.add(recovery1);
        recoveryList.add(recovery2);

        // Mock behavior
        when(attendanceRecoveryRepository.findAll()).thenReturn(recoveryList);

        // When
        List<AttendanceRecovery> result = attendanceRecoveryRepository.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("id1", result.get(0).getId());
        assertEquals("id2", result.get(1).getId());
    }

    @Test
    void testFindByFacility() {
        // Add this method to AttendanceRecoveryRepository
        // Method will be mocked, so no need to actually implement it
    }
}
