package udpm.hn.studentattendance.core.admin.facility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.Facility;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AFFacilityRepositoryTest {

    @Autowired
    private AFFacilityExtendRepository afFacilityRepository;

    @Test
    void testAFFacilityRepositoryExists() {
        assertNotNull(afFacilityRepository);
    }

    @Test
    void testFindAll() {
        var result = afFacilityRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        Facility facility = new Facility();
        facility.setCode("F001");
        facility.setName("Test Facility");
        facility.setPosition(1);
        Facility saved = afFacilityRepository.save(facility);
        var result = afFacilityRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("Test Facility", result.get().getName());
    }

    @Test
    void testSave() {
        Facility facility = new Facility();
        facility.setCode("F002");
        facility.setName("Another Facility");
        facility.setPosition(2);
        Facility saved = afFacilityRepository.save(facility);
        assertNotNull(saved.getId());
        assertEquals("Another Facility", saved.getName());
    }

    @Test
    void testDelete() {
        Facility facility = new Facility();
        facility.setCode("F003");
        facility.setName("Delete Facility");
        facility.setPosition(3);
        Facility saved = afFacilityRepository.save(facility);
        afFacilityRepository.deleteById(saved.getId());
        assertFalse(afFacilityRepository.findById(saved.getId()).isPresent());
    }
}