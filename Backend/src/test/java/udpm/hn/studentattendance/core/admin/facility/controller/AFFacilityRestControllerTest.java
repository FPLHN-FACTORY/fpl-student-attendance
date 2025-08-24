package udpm.hn.studentattendance.core.admin.facility.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AFFacilityRestControllerTest {
    @Mock
    private AFFacilityService adFacilityService;

    @InjectMocks
    private AFFacilityRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllFacility() {
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        when(adFacilityService.getAllFacility(request)).thenReturn((ResponseEntity) ResponseEntity.ok("facilities"));
        ResponseEntity<?> res = controller.getAllFacility(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("facilities", res.getBody());
    }

    @Test
    void testGetFacility() {
        String facilityId = "123";
        when(adFacilityService.getFacilityById(facilityId)).thenReturn((ResponseEntity) ResponseEntity.ok("facility"));
        ResponseEntity<?> res = controller.getFacility(facilityId);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("facility", res.getBody());
    }

    @Test
    void testCreateFacility() {
        AFCreateUpdateFacilityRequest request = new AFCreateUpdateFacilityRequest();
        when(adFacilityService.createFacility(request)).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.createFacility(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testUpdateFacility() {
        String facilityId = "123";
        AFCreateUpdateFacilityRequest request = new AFCreateUpdateFacilityRequest();
        when(adFacilityService.updateFacility(facilityId, request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateFacility(facilityId, request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testChangeStatusFacility() {
        String facilityId = "123";
        when(adFacilityService.changeFacilityStatus(facilityId))
                .thenReturn((ResponseEntity) ResponseEntity.ok("statusChanged"));
        ResponseEntity<?> res = controller.changeStatusFacility(facilityId);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("statusChanged", res.getBody());
    }

    @Test
    void testUp() {
        String facilityId = "123";
        when(adFacilityService.up(facilityId)).thenReturn((ResponseEntity) ResponseEntity.ok("up"));
        ResponseEntity<?> res = controller.up(facilityId);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("up", res.getBody());
    }

    @Test
    void testDown() {
        String facilityId = "123";
        when(adFacilityService.down(facilityId)).thenReturn((ResponseEntity) ResponseEntity.ok("down"));
        ResponseEntity<?> res = controller.down(facilityId);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("down", res.getBody());
    }
}
