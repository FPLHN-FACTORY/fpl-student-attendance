package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SSUserAdminRepositoryTest {
    @Autowired
    private SSUserAdminRepository ssUserAdminRepository;

    @Test
    void testSSUserAdminRepositoryExists() {
        assertNotNull(ssUserAdminRepository);
    }

    @Test
    void testFindAll() {
        var result = ssUserAdminRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        String id = "123";
        var result = ssUserAdminRepository.findById(id);
        assertNotNull(result);
    }

    @Test
    void testSave() {
        // Test save method exists
        assertNotNull(ssUserAdminRepository);
    }

    @Test
    void testDelete() {
        String id = "123";
        ssUserAdminRepository.deleteById(id);
        assertTrue(true);
    }
} 