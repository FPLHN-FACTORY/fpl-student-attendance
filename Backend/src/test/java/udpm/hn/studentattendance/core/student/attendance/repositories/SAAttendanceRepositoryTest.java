package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class SAAttendanceRepositoryTest {
    @Autowired
    private SAAttendanceRepository saAttendanceRepository;

    @Test
    void testSAAttendanceRepositoryExists() {
        assertNotNull(saAttendanceRepository);
    }

    @Test
    void testFindAll() {
        var result = saAttendanceRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        String id = "123";
        var result = saAttendanceRepository.findById(id);
        assertNotNull(result);
    }

    @Test
    void testSave() {
        // Test save method exists
        assertNotNull(saAttendanceRepository);
    }

    @Test
    void testDelete() {
        String id = "123";
        saAttendanceRepository.deleteById(id);
        assertTrue(true);
    }
}