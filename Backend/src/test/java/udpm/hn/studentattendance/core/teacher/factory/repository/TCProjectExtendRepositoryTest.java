package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TCProjectExtendRepositoryTest {
    @Autowired
    private TCProjectExtendRepository tcProjectExtendRepository;

    @Test
    void testTCProjectExtendRepositoryExists() {
        assertNotNull(tcProjectExtendRepository);
    }

    @Test
    void testGetAllProjectName() {
        // Chỉ test gọi method, không kiểm tra dữ liệu thực tế vì không có data mẫu
        assertDoesNotThrow(() -> tcProjectExtendRepository.getAllProjectName("test-facility-id"));
    }
}