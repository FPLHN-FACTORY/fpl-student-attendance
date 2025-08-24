package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADSubjectFacilityRepositoryTest {
    @Autowired
    private ADSubjectFacilityRepository adSubjectFacilityRepository;

    @Test
    void testADSubjectFacilityRepositoryExists() {
        assertNotNull(adSubjectFacilityRepository);
    }

    @Test
    void testFindAll() {
        var result = adSubjectFacilityRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Facility facility = new Facility();
        facility.setCode("FAC004");
        facility.setName("CÆ¡ sá»Ÿ 4");
        facility.setPosition(4);
        Subject subject = new Subject();
        subject.setCode("SUBJ004");
        subject.setName("Tin há»c Ä‘áº¡i cÆ°Æ¡ng");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        SubjectFacility saved = adSubjectFacilityRepository.save(subjectFacility);
        var result = adSubjectFacilityRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("FAC004", result.get().getFacility().getCode());
        assertEquals("SUBJ004", result.get().getSubject().getCode());
    }

    @Test
    void testSave() {
        Facility facility = new Facility();
        facility.setCode("FAC005");
        facility.setName("CÆ¡ sá»Ÿ 5");
        facility.setPosition(5);
        Subject subject = new Subject();
        subject.setCode("SUBJ005");
        subject.setName("Ká»¹ thuáº­t láº­p trÃ¬nh");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        SubjectFacility saved = adSubjectFacilityRepository.save(subjectFacility);
        assertNotNull(saved.getId());
        assertEquals("FAC005", saved.getFacility().getCode());
        assertEquals("SUBJ005", saved.getSubject().getCode());
    }

    @Test
    void testDelete() {
        Facility facility = new Facility();
        facility.setCode("FAC006");
        facility.setName("CÆ¡ sá»Ÿ 6");
        facility.setPosition(6);
        Subject subject = new Subject();
        subject.setCode("SUBJ006");
        subject.setName("CÆ¡ sá»Ÿ dá»¯ liá»‡u");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        SubjectFacility saved = adSubjectFacilityRepository.save(subjectFacility);
        adSubjectFacilityRepository.deleteById(saved.getId());
        assertFalse(adSubjectFacilityRepository.findById(saved.getId()).isPresent());
    }
}
