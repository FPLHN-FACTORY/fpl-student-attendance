package udpm.hn.studentattendance.core.admin.facility.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityIPService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AFFacilityIPRestControllerTest {
    @Mock
    private AFFacilityIPService afFacilityIPService;

    @InjectMocks
    private AFFacilityIPRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        String idFacility = "123";
        when(afFacilityIPService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testAddIP() {
        AFAddOrUpdateFacilityIPRequest request = new AFAddOrUpdateFacilityIPRequest();
        String idFacility = "123";
        when(afFacilityIPService.addIP(request)).thenReturn((ResponseEntity) ResponseEntity.ok("added"));
        ResponseEntity<?> res = controller.addIP(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("added", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testUpdateIP() {
        AFAddOrUpdateFacilityIPRequest request = new AFAddOrUpdateFacilityIPRequest();
        String idFacility = "123";
        when(afFacilityIPService.updateIP(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateIP(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testDeleteIP() {
        String idFacilityIP = "123";
        when(afFacilityIPService.deleteIP(idFacilityIP)).thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deleteIP(idFacilityIP);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testChangeStatus() {
        String idFacilityIP = "123";
        when(afFacilityIPService.changeStatus(idFacilityIP))
                .thenReturn((ResponseEntity) ResponseEntity.ok("statusChanged"));
        ResponseEntity<?> res = controller.changeStatus(idFacilityIP);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("statusChanged", res.getBody());
    }
}