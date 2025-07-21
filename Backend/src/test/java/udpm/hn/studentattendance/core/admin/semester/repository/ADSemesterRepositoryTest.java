package udpm.hn.studentattendance.core.admin.semester.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADSemesterRepositoryTest {
    @Autowired
    private ADSemesterRepository adSemesterRepository;

    @Test
    void testADSemesterRepositoryExists() {
        assertNotNull(adSemesterRepository);
    }

    @Test
    void testFindAll() {
        var result = adSemesterRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Semester semester = new Semester();
        semester.setCode("HK1-2024");
        semester.setSemesterName(SemesterName.SPRING);
        semester.setFromDate(System.currentTimeMillis());
        semester.setToDate(System.currentTimeMillis() + 1000000L);
        semester.setYear(2024);
        Semester saved = adSemesterRepository.save(semester);
        var result = adSemesterRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("HK1-2024", result.get().getCode());
    }

    @Test
    void testSave() {
        Semester semester = new Semester();
        semester.setCode("HK2-2024");
        semester.setSemesterName(SemesterName.SUMMER);
        semester.setFromDate(System.currentTimeMillis());
        semester.setToDate(System.currentTimeMillis() + 1000000L);
        semester.setYear(2024);
        Semester saved = adSemesterRepository.save(semester);
        assertNotNull(saved.getId());
        assertEquals("HK2-2024", saved.getCode());
    }

    @Test
    void testDelete() {
        Semester semester = new Semester();
        semester.setCode("HK3-2024");
        semester.setSemesterName(SemesterName.FALL);
        semester.setFromDate(System.currentTimeMillis());
        semester.setToDate(System.currentTimeMillis() + 1000000L);
        semester.setYear(2024);
        Semester saved = adSemesterRepository.save(semester);
        adSemesterRepository.deleteById(saved.getId());
        assertFalse(adSemesterRepository.findById(saved.getId()).isPresent());
    }
}
