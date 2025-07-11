package udpm.hn.studentattendance.core.admin.useradmin.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADUserAdminExtendRepositoryTest {
    @Autowired
    private ADUserAdminExtendRepository adUserAdminExtendRepository;

    @Test
    void testADUserAdminExtendRepositoryExists() {
        assertNotNull(adUserAdminExtendRepository);
    }

    @Test
    void testFindAll() {
        var result = adUserAdminExtendRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        UserAdmin admin = new UserAdmin();
        admin.setCode("A001");
        admin.setEmail("admin@gmail.com");
        admin.setName("Nguyen Van A");
        admin.setImage("image.png");
        UserAdmin saved = adUserAdminExtendRepository.save(admin);
        var result = adUserAdminExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("A001", result.get().getCode());
    }

    @Test
    void testSave() {
        UserAdmin admin = new UserAdmin();
        admin.setCode("A002");
        admin.setEmail("admin2@gmail.com");
        admin.setName("Nguyen Van B");
        admin.setImage("image2.png");
        UserAdmin saved = adUserAdminExtendRepository.save(admin);
        assertNotNull(saved.getId());
        assertEquals("A002", saved.getCode());
    }

    @Test
    void testDelete() {
        UserAdmin admin = new UserAdmin();
        admin.setCode("A003");
        admin.setEmail("admin3@gmail.com");
        admin.setName("Nguyen Van C");
        admin.setImage("image3.png");
        UserAdmin saved = adUserAdminExtendRepository.save(admin);
        adUserAdminExtendRepository.deleteById(saved.getId());
        assertFalse(adUserAdminExtendRepository.findById(saved.getId()).isPresent());
    }
}