package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADSubjectRepositoryTest {
    @Autowired
    private ADSubjectRepository adSubjectRepository;

    @Test
    void testADSubjectRepositoryExists() {
        assertNotNull(adSubjectRepository);
    }

    @Test
    void testFindAll() {
        var result = adSubjectRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Subject subject = new Subject();
        subject.setCode("SUBJ001");
        subject.setName("ToÃ¡n cao cáº¥p");
        Subject saved = adSubjectRepository.save(subject);
        var result = adSubjectRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("SUBJ001", result.get().getCode());
    }

    @Test
    void testSave() {
        Subject subject = new Subject();
        subject.setCode("SUBJ002");
        subject.setName("Váº­t lÃ½ Ä‘áº¡i cÆ°Æ¡ng");
        Subject saved = adSubjectRepository.save(subject);
        assertNotNull(saved.getId());
        assertEquals("SUBJ002", saved.getCode());
    }

    @Test
    void testDelete() {
        Subject subject = new Subject();
        subject.setCode("SUBJ003");
        subject.setName("HÃ³a há»c cÆ¡ báº£n");
        Subject saved = adSubjectRepository.save(subject);
        adSubjectRepository.deleteById(saved.getId());
        assertFalse(adSubjectRepository.findById(saved.getId()).isPresent());
    }
}
