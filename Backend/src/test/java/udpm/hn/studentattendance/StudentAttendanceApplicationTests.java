package udpm.hn.studentattendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StudentAttendanceApplicationTests {

    @Test
    @DisplayName("Application Context Loads Successfully")
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }

}
