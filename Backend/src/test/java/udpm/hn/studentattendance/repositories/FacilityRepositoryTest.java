package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Facility;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class FacilityRepositoryTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("FPT HCM");
        facility.setPosition(1);

        // When
        Facility savedFacility = facilityRepository.save(facility);
        Optional<Facility> foundFacility = facilityRepository.findById(savedFacility.getId());

        // Then
        assertTrue(foundFacility.isPresent());
        assertEquals("FAC001", foundFacility.get().getCode());
        assertEquals("FPT HCM", foundFacility.get().getName());
        assertEquals(1, foundFacility.get().getPosition());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Facility facility1 = new Facility();
        facility1.setCode("FAC001");
        facility1.setName("FPT HCM");
        facility1.setPosition(1);

        Facility facility2 = new Facility();
        facility2.setCode("FAC002");
        facility2.setName("FPT Hanoi");
        facility2.setPosition(2);

        // When
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);
        List<Facility> allFacilities = facilityRepository.findAll();

        // Then
        assertTrue(allFacilities.size() >= 2);
        assertTrue(allFacilities.stream().anyMatch(f -> "FAC001".equals(f.getCode())));
        assertTrue(allFacilities.stream().anyMatch(f -> "FAC002".equals(f.getCode())));
    }

    @Test
    void testUpdateFacility() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("Original Name");
        facility.setPosition(1);

        Facility savedFacility = facilityRepository.save(facility);

        // When
        savedFacility.setName("Updated Name");
        savedFacility.setPosition(2);
        Facility updatedFacility = facilityRepository.save(savedFacility);

        // Then
        assertEquals("Updated Name", updatedFacility.getName());
        assertEquals(2, updatedFacility.getPosition());
        assertEquals("FAC001", updatedFacility.getCode());
    }

    @Test
    void testDeleteFacility() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("Facility to Delete");
        facility.setPosition(1);

        Facility savedFacility = facilityRepository.save(facility);
        String facilityId = savedFacility.getId();

        // When
        facilityRepository.deleteById(facilityId);
        Optional<Facility> deletedFacility = facilityRepository.findById(facilityId);

        // Then
        assertFalse(deletedFacility.isPresent());
    }
}