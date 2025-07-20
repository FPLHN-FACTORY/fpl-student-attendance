package udpm.hn.studentattendance.core.admin.facility.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityLocationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AFFacilityLocationRestControllerTest {
    @Mock
    private AFFacilityLocationService afFacilityLocationService;

    @InjectMocks
    private AFFacilityLocationRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityLocationRequest request = new AFFilterFacilityLocationRequest();
        String idFacility = "123";
        when(afFacilityLocationService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testAddLocation() {
        AFAddOrUpdateFacilityLocationRequest request = new AFAddOrUpdateFacilityLocationRequest();
        String idFacility = "123";
        when(afFacilityLocationService.addLocation(request)).thenReturn((ResponseEntity) ResponseEntity.ok("added"));
        ResponseEntity<?> res = controller.addIP(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("added", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testUpdateLocation() {
        AFAddOrUpdateFacilityLocationRequest request = new AFAddOrUpdateFacilityLocationRequest();
        String idFacility = "123";
        when(afFacilityLocationService.updateLocation(request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateIP(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testDeleteLocation() {
        String idFacilityLocation = "123";
        when(afFacilityLocationService.deleteLocation(idFacilityLocation))
                .thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deleteIP(idFacilityLocation);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testChangeStatus() {
        String idFacilityLocation = "123";
        when(afFacilityLocationService.changeStatus(idFacilityLocation))
                .thenReturn((ResponseEntity) ResponseEntity.ok("statusChanged"));
        ResponseEntity<?> res = controller.changeStatus(idFacilityLocation);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("statusChanged", res.getBody());
    }
}
