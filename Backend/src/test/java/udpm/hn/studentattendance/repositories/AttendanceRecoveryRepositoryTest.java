package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.AttendanceRecovery;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class AttendanceRecoveryRepositoryTest {
    
    @Test
    void testAttendanceRecoveryRepositoryExists() {
        assertNotNull(AttendanceRecoveryRepository.class);
    }

    @Test
    void testFindByStudentAndSubject() throws Exception {
        assertNotNull(AttendanceRecoveryRepository.class.getMethod("findByStudentAndSubject", 
            Class.forName("udpm.hn.studentattendance.entities.Student"), 
            Class.forName("udpm.hn.studentattendance.entities.Subject")));
    }

    @Test
    void testFindByStudentAndSubjectAndDateBetween() throws Exception {
        assertNotNull(AttendanceRecoveryRepository.class.getMethod("findByStudentAndSubjectAndDateBetween", 
            Class.forName("udpm.hn.studentattendance.entities.Student"), 
            Class.forName("udpm.hn.studentattendance.entities.Subject"), 
            LocalDateTime.class, LocalDateTime.class));
    }

    @Test
    void testFindByStatus() throws Exception {
        assertNotNull(AttendanceRecoveryRepository.class.getMethod("findByStatus", 
            Class.forName("udpm.hn.studentattendance.infrastructure.constants.EntityStatus")));
    }

    @Test
    void testFindByStudentAndStatus() throws Exception {
        assertNotNull(AttendanceRecoveryRepository.class.getMethod("findByStudentAndStatus", 
            Class.forName("udpm.hn.studentattendance.entities.Student"), 
            Class.forName("udpm.hn.studentattendance.infrastructure.constants.EntityStatus")));
    }
}