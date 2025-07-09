package udpm.hn.studentattendance.core.admin.facility.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AFFacilityRestControllerTest {
    @Mock
    private AFFacilityService afFacilityService;

    @InjectMocks
    private AFFacilityRestController afFacilityRestController;

    @Test
    void testControllerExists() {
        assertNotNull(afFacilityRestController);
    }

    @Test
    void testGetAllFacilities() {
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        when(afFacilityService.getAllFacility(request)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = afFacilityRestController.getAllFacility(request);

        assertNotNull(response);
        verify(afFacilityService).getAllFacility(request);
    }

    @Test
    void testGetFacilityById() {
        String facilityId = "123";
        when(afFacilityService.getFacilityById(facilityId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = afFacilityRestController.getFacility(facilityId);

        assertNotNull(response);
        verify(afFacilityService).getFacilityById(facilityId);
    }

    @Test
    void testCreateFacility() {
        AFCreateUpdateFacilityRequest facilityRequest = new AFCreateUpdateFacilityRequest();
        when(afFacilityService.createFacility(facilityRequest)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = afFacilityRestController.createFacility(facilityRequest);

        assertNotNull(response);
        verify(afFacilityService).createFacility(facilityRequest);
    }

    @Test
    void testUpdateFacility() {
        String facilityId = "123";
        AFCreateUpdateFacilityRequest facilityRequest = new AFCreateUpdateFacilityRequest();
        when(afFacilityService.updateFacility(facilityId, facilityRequest)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = afFacilityRestController.updateFacility(facilityId, facilityRequest);

        assertNotNull(response);
        verify(afFacilityService).updateFacility(facilityId, facilityRequest);
    }

    @Test
    void testDeleteFacility() {
        // No delete endpoint in AFFacilityRestController, so this test is removed.
    }
}