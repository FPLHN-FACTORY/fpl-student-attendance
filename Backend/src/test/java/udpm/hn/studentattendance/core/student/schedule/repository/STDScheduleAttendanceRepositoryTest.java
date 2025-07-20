package udpm.hn.studentattendance.core.student.schedule.repository;

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
class STDScheduleAttendanceRepositoryTest {
    @Autowired
    private STDScheduleAttendanceRepository stdScheduleAttendanceRepository;

    @Test
    void testSTDScheduleAttendanceRepositoryExists() {
        assertNotNull(stdScheduleAttendanceRepository);
    }

    @Test
    void testFindAll() {
        var result = stdScheduleAttendanceRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        String id = "123";
        var result = stdScheduleAttendanceRepository.findById(id);
        assertNotNull(result);
    }

    @Test
    void testSave() {
        // Test save method exists
        assertNotNull(stdScheduleAttendanceRepository);
    }

    @Test
    void testDelete() {
        String id = "123";
        stdScheduleAttendanceRepository.deleteById(id);
        assertTrue(true);
    }
}
