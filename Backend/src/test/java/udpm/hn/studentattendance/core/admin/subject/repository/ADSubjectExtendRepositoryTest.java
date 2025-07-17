package udpm.hn.studentattendance.core.admin.subject.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADSubjectExtendRepositoryTest {
    @Autowired
    private ADSubjectExtendRepository adSubjectExtendRepository;

    @Test
    void testADSubjectExtendRepositoryExists() {
        assertNotNull(adSubjectExtendRepository);
    }

    @Test
    void testFindAll() {
        var result = adSubjectExtendRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Subject subject = new Subject();
        subject.setCode("MATH101");
        subject.setName("ToÃ¡n cao cáº¥p");
        Subject saved = adSubjectExtendRepository.save(subject);
        var result = adSubjectExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("MATH101", result.get().getCode());
    }

    @Test
    void testSave() {
        Subject subject = new Subject();
        subject.setCode("PHYS101");
        subject.setName("Váº­t lÃ½ Ä‘áº¡i cÆ°Æ¡ng");
        Subject saved = adSubjectExtendRepository.save(subject);
        assertNotNull(saved.getId());
        assertEquals("PHYS101", saved.getCode());
    }

    @Test
    void testDelete() {
        Subject subject = new Subject();
        subject.setCode("CHEM101");
        subject.setName("HÃ³a há»c cÆ¡ báº£n");
        Subject saved = adSubjectExtendRepository.save(subject);
        adSubjectExtendRepository.deleteById(saved.getId());
        assertFalse(adSubjectExtendRepository.findById(saved.getId()).isPresent());
    }
}