package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Test
    void testRepositoryExists() {
        assertNotNull(attendanceRepository);
    }

    @Test
    void testSaveAndFindAll() {
        // Create test attendance
        Attendance attendance = new Attendance();
        attendance.setStatus(EntityStatus.ACTIVE);

        // Save
        Attendance saved = attendanceRepository.save(attendance);
        assertNotNull(saved.getId());

        // Find all
        List<Attendance> all = attendanceRepository.findAll();
        assertFalse(all.isEmpty());
    }

    @Test
    void testFindById() {
        // Create and save
        Attendance attendance = new Attendance();
        attendance.setStatus(EntityStatus.ACTIVE);
        Attendance saved = attendanceRepository.save(attendance);

        // Find by id
        Optional<Attendance> found = attendanceRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testDeleteById() {
        // Create and save
        Attendance attendance = new Attendance();
        attendance.setStatus(EntityStatus.ACTIVE);
        Attendance saved = attendanceRepository.save(attendance);

        // Delete
        attendanceRepository.deleteById(saved.getId());

        // Verify deleted
        Optional<Attendance> found = attendanceRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}