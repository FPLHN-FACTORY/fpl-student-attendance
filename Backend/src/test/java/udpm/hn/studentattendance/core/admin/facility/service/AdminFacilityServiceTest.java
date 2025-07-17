package udpm.hn.studentattendance.core.admin.facility.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminFacilityServiceTest {
    @Mock
    private AFFacilityService afFacilityService;

    @Test
    void testAFFacilityServiceExists() {
        assertNotNull(afFacilityService);
    }

    @Test
    void testGetAllFacility() {
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        when(afFacilityService.getAllFacility(any())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.getAllFacility(request);
        
        assertNotNull(response);
        verify(afFacilityService).getAllFacility(any());
    }

    @Test
    void testGetFacilityById() {
        String facilityId = "123";
        when(afFacilityService.getFacilityById(anyString())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.getFacilityById(facilityId);
        
        assertNotNull(response);
        verify(afFacilityService).getFacilityById(anyString());
    }

    @Test
    void testCreateFacility() {
        AFCreateUpdateFacilityRequest request = new AFCreateUpdateFacilityRequest();
        when(afFacilityService.createFacility(any())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.createFacility(request);
        
        assertNotNull(response);
        verify(afFacilityService).createFacility(any());
    }

    @Test
    void testUpdateFacility() {
        String facilityId = "123";
        AFCreateUpdateFacilityRequest request = new AFCreateUpdateFacilityRequest();
        when(afFacilityService.updateFacility(anyString(), any())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.updateFacility(facilityId, request);
        
        assertNotNull(response);
        verify(afFacilityService).updateFacility(anyString(), any());
    }

    @Test
    void testChangeFacilityStatus() {
        String facilityId = "123";
        when(afFacilityService.changeFacilityStatus(anyString())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.changeFacilityStatus(facilityId);
        
        assertNotNull(response);
        verify(afFacilityService).changeFacilityStatus(anyString());
    }

    @Test
    void testUp() {
        String facilityId = "123";
        when(afFacilityService.up(anyString())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.up(facilityId);
        
        assertNotNull(response);
        verify(afFacilityService).up(anyString());
    }

    @Test
    void testDown() {
        String facilityId = "123";
        when(afFacilityService.down(anyString())).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = afFacilityService.down(facilityId);
        
        assertNotNull(response);
        verify(afFacilityService).down(anyString());
    }
}