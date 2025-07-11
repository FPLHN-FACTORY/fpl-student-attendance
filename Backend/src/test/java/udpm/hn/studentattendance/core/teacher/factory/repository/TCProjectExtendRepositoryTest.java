package udpm.hn.studentattendance.core.teacher.factory.repository;

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
class TCProjectExtendRepositoryTest {
    @Autowired
    private TCProjectExtendRepository tcProjectExtendRepository;

    @Test
    void testTCProjectExtendRepositoryExists() {
        assertNotNull(tcProjectExtendRepository);
    }

    @Test
    void testGetAllProjectName() {
        // Chá»‰ test gá»i method, khÃ´ng kiá»ƒm tra dá»¯ liá»‡u thá»±c táº¿ vÃ¬ khÃ´ng cÃ³ data máº«u
        assertDoesNotThrow(() -> tcProjectExtendRepository.getAllProjectName("test-facility-id"));
    }
}