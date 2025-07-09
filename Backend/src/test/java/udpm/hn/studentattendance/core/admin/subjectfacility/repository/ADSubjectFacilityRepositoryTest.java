package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Subject;

@DataJpaTest
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
        facility.setName("Cơ sở 4");
        facility.setPosition(4);
        Subject subject = new Subject();
        subject.setCode("SUBJ004");
        subject.setName("Tin học đại cương");
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
        facility.setName("Cơ sở 5");
        facility.setPosition(5);
        Subject subject = new Subject();
        subject.setCode("SUBJ005");
        subject.setName("Kỹ thuật lập trình");
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
        facility.setName("Cơ sở 6");
        facility.setPosition(6);
        Subject subject = new Subject();
        subject.setCode("SUBJ006");
        subject.setName("Cơ sở dữ liệu");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        SubjectFacility saved = adSubjectFacilityRepository.save(subjectFacility);
        adSubjectFacilityRepository.deleteById(saved.getId());
        assertFalse(adSubjectFacilityRepository.findById(saved.getId()).isPresent());
    }
}