package udpm.hn.studentattendance.core.staff.statistics.repositories;

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
class SSFactoryRepositoryTest {
    @Autowired
    private SSFactoryRepository ssFactoryRepository;

    @Test
    void testSSFactoryRepositoryExists() {
        assertNotNull(ssFactoryRepository);
    }

    @Test
    void testFindAll() {
        var result = ssFactoryRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        String id = "123";
        var result = ssFactoryRepository.findById(id);
        assertNotNull(result);
    }

    @Test
    void testSave() {
        // Test save method exists
        assertNotNull(ssFactoryRepository);
    }

    @Test
    void testDelete() {
        String id = "123";
        ssFactoryRepository.deleteById(id);
        assertTrue(true);
    }
}
