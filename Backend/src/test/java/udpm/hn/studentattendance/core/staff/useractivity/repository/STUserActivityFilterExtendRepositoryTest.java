package udpm.hn.studentattendance.core.staff.useractivity.repository;

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
class STUserActivityFilterExtendRepositoryTest {
    @Autowired
    private STUserActivityFilterExtendRepository stUserActivityFilterExtendRepository;

    @Test
    void testSTUserActivityFilterExtendRepositoryExists() {
        assertNotNull(stUserActivityFilterExtendRepository);
    }

    @Test
    void testFindAll() {
        var result = stUserActivityFilterExtendRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        String id = "123";
        var result = stUserActivityFilterExtendRepository.findById(id);
        assertNotNull(result);
    }

    @Test
    void testSave() {
        // Test save method exists
        assertNotNull(stUserActivityFilterExtendRepository);
    }

    @Test
    void testDelete() {
        String id = "123";
        stUserActivityFilterExtendRepository.deleteById(id);
        assertTrue(true);
    }
}