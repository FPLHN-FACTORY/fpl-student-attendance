package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.Facility;

@DataJpaTest
@ActiveProfiles("test")
class ADFacilityRepositoryTest {
    @Autowired
    private ADFacilityRepository adFacilityRepository;

    @Test
    void testADFacilityRepositoryExists() {
        assertNotNull(adFacilityRepository);
    }

    @Test
    void testFindAll() {
        var result = adFacilityRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("Cơ sở 1");
        facility.setPosition(1);
        Facility saved = adFacilityRepository.save(facility);
        var result = adFacilityRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("FAC001", result.get().getCode());
    }

    @Test
    void testSave() {
        Facility facility = new Facility();
        facility.setCode("FAC002");
        facility.setName("Cơ sở 2");
        facility.setPosition(2);
        Facility saved = adFacilityRepository.save(facility);
        assertNotNull(saved.getId());
        assertEquals("FAC002", saved.getCode());
    }

    @Test
    void testDelete() {
        Facility facility = new Facility();
        facility.setCode("FAC003");
        facility.setName("Cơ sở 3");
        facility.setPosition(3);
        Facility saved = adFacilityRepository.save(facility);
        adFacilityRepository.deleteById(saved.getId());
        assertFalse(adFacilityRepository.findById(saved.getId()).isPresent());
    }
}