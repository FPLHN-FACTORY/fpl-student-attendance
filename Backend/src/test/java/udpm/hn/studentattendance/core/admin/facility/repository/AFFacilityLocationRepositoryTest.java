package udpm.hn.studentattendance.core.admin.facility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AFFacilityLocationRepositoryTest {
    @Autowired
    private AFFacilityLocationRepository afFacilityLocationRepository;

    @Test
    void testAFFacilityLocationRepositoryExists() {
        assertNotNull(afFacilityLocationRepository);
    }

    @Test
    void testFindAll() {
        var result = afFacilityLocationRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        FacilityLocation location = new FacilityLocation();
        location.setName("A1");
        location.setLatitude(21.028511);
        location.setLongitude(105.804817);
        location.setRadius(100);
        location.setStatus(EntityStatus.ACTIVE);
        FacilityLocation saved = afFacilityLocationRepository.save(location);
        var result = afFacilityLocationRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getName());
    }

    @Test
    void testSave() {
        FacilityLocation location = new FacilityLocation();
        location.setName("B2");
        location.setLatitude(21.028511);
        location.setLongitude(105.804817);
        location.setRadius(200);
        location.setStatus(EntityStatus.ACTIVE);
        FacilityLocation saved = afFacilityLocationRepository.save(location);
        assertNotNull(saved.getId());
        assertEquals("B2", saved.getName());
    }

    @Test
    void testDelete() {
        FacilityLocation location = new FacilityLocation();
        location.setName("C3");
        location.setLatitude(21.028511);
        location.setLongitude(105.804817);
        location.setRadius(300);
        location.setStatus(EntityStatus.ACTIVE);
        FacilityLocation saved = afFacilityLocationRepository.save(location);
        afFacilityLocationRepository.deleteById(saved.getId());
        assertFalse(afFacilityLocationRepository.findById(saved.getId()).isPresent());
    }
}