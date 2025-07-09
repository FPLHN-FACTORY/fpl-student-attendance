package udpm.hn.studentattendance.core.admin.useractivity.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.UserActivityLog;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ADUserActivityFilterExtendRepositoryTest {
    @Autowired
    private ADUserActivityFilterExtendRepository adUserActivityFilterExtendRepository;

    @Test
    void testADUserActivityFilterExtendRepositoryExists() {
        assertNotNull(adUserActivityFilterExtendRepository);
    }

    @Test
    void testFindAll() {
        var result = adUserActivityFilterExtendRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user1");
        log.setRole(RoleConstant.ADMIN);
        log.setMessage("Test log");
        UserActivityLog saved = adUserActivityFilterExtendRepository.save(log);
        var result = adUserActivityFilterExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getIdUser());
    }

    @Test
    void testSave() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user2");
        log.setRole(RoleConstant.STAFF);
        log.setMessage("Save log");
        UserActivityLog saved = adUserActivityFilterExtendRepository.save(log);
        assertNotNull(saved.getId());
        assertEquals("user2", saved.getIdUser());
    }

    @Test
    void testDelete() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user3");
        log.setRole(RoleConstant.TEACHER);
        log.setMessage("Delete log");
        UserActivityLog saved = adUserActivityFilterExtendRepository.save(log);
        adUserActivityFilterExtendRepository.deleteById(saved.getId());
        assertFalse(adUserActivityFilterExtendRepository.findById(saved.getId()).isPresent());
    }
}