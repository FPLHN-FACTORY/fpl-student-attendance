package udpm.hn.studentattendance.core.admin.levelproject.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADLevelProjectRepositoryTest {
    @Autowired
    private ADLevelProjectRepository adLevelProjectRepository;

    @Test
    void testADLevelProjectRepositoryExists() {
        assertNotNull(adLevelProjectRepository);
    }

    @Test
    void testFindAll() {
        var result = adLevelProjectRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        LevelProject entity = new LevelProject();
        entity.setCode("LP001");
        entity.setName("Cáº¥p Ä‘á»™ 1");
        entity.setDescription("MÃ´ táº£ cáº¥p Ä‘á»™ 1");
        LevelProject saved = adLevelProjectRepository.save(entity);
        var result = adLevelProjectRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("LP001", result.get().getCode());
    }

    @Test
    void testSave() {
        LevelProject entity = new LevelProject();
        entity.setCode("LP002");
        entity.setName("Cáº¥p Ä‘á»™ 2");
        entity.setDescription("MÃ´ táº£ cáº¥p Ä‘á»™ 2");
        LevelProject saved = adLevelProjectRepository.save(entity);
        assertNotNull(saved.getId());
        assertEquals("LP002", saved.getCode());
    }

    @Test
    void testDelete() {
        LevelProject entity = new LevelProject();
        entity.setCode("LP003");
        entity.setName("Cáº¥p Ä‘á»™ 3");
        entity.setDescription("MÃ´ táº£ cáº¥p Ä‘á»™ 3");
        LevelProject saved = adLevelProjectRepository.save(entity);
        adLevelProjectRepository.deleteById(saved.getId());
        assertFalse(adLevelProjectRepository.findById(saved.getId()).isPresent());
    }
}