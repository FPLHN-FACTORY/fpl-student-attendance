package udpm.hn.studentattendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.MockDBGenerator;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockDBGenerator.class)
class StudentAttendanceApplicationTests {

    @Test
    @DisplayName("Application Context Loads Successfully")
    void contextLoads() {
    }

}
