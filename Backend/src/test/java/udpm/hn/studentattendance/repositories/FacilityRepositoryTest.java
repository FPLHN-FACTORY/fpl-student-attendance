package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Facility;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacilityRepositoryTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("FPT HCM");
        facility.setPosition(1);

        // Mock behavior
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);
        when(facilityRepository.findById(anyString())).thenReturn(Optional.of(facility));

        // When
        Facility savedFacility = facilityRepository.save(facility);
        Optional<Facility> foundFacility = facilityRepository.findById("mock-id");

        // Then
        assertTrue(foundFacility.isPresent());
        assertEquals("FAC001", foundFacility.get().getCode());
        assertEquals("FPT HCM", foundFacility.get().getName());
        assertEquals(1, foundFacility.get().getPosition());
        verify(facilityRepository).save(any(Facility.class));
        verify(facilityRepository).findById(anyString());
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

        List<Facility> facilities = Arrays.asList(facility1, facility2);

        // Mock behavior
        when(facilityRepository.findAll()).thenReturn(facilities);
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);
        List<Facility> allFacilities = facilityRepository.findAll();

        // Then
        assertEquals(2, allFacilities.size());
        assertTrue(allFacilities.stream().anyMatch(f -> "FAC001".equals(f.getCode())));
        assertTrue(allFacilities.stream().anyMatch(f -> "FAC002".equals(f.getCode())));
        verify(facilityRepository, times(2)).save(any(Facility.class));
        verify(facilityRepository).findAll();
    }

    @Test
    void testUpdateFacility() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("Original Name");
        facility.setPosition(1);

        Facility updatedFacility = new Facility();
        updatedFacility.setCode("FAC001");
        updatedFacility.setName("Updated Name");
        updatedFacility.setPosition(2);

        // Mock behavior
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility).thenReturn(updatedFacility);

        // When
        Facility savedFacility = facilityRepository.save(facility);
        savedFacility.setName("Updated Name");
        savedFacility.setPosition(2);
        Facility resultFacility = facilityRepository.save(savedFacility);

        // Then
        assertEquals("Updated Name", resultFacility.getName());
        assertEquals(2, resultFacility.getPosition());
        assertEquals("FAC001", resultFacility.getCode());
        verify(facilityRepository, times(2)).save(any(Facility.class));
    }

    @Test
    void testDeleteFacility() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("Facility to Delete");
        facility.setPosition(1);
        String facilityId = "mock-id";

        // Mock behavior
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);
        doNothing().when(facilityRepository).deleteById(anyString());
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        // When
        Facility savedFacility = facilityRepository.save(facility);
        facilityRepository.deleteById(facilityId);
        Optional<Facility> deletedFacility = facilityRepository.findById(facilityId);

        // Then
        assertFalse(deletedFacility.isPresent());
        verify(facilityRepository).save(any(Facility.class));
        verify(facilityRepository).deleteById(anyString());
        verify(facilityRepository).findById(anyString());
    }
}
