package udpm.hn.studentattendance.core.admin.facility.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityShiftService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AFFacilityShiftRestControllerTest {
    @Mock
    private AFFacilityShiftService afFacilityShiftService;

    @InjectMocks
    private AFFacilityShiftRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        String idFacility = "123";
        when(afFacilityShiftService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testAddShift() {
        AFAddOrUpdateFacilityShiftRequest request = new AFAddOrUpdateFacilityShiftRequest();
        String idFacility = "123";
        when(afFacilityShiftService.addShift(request)).thenReturn((ResponseEntity) ResponseEntity.ok("added"));
        ResponseEntity<?> res = controller.add(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("added", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testUpdateShift() {
        AFAddOrUpdateFacilityShiftRequest request = new AFAddOrUpdateFacilityShiftRequest();
        String idFacility = "123";
        when(afFacilityShiftService.updateShift(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.update(request, idFacility);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
        assertEquals(idFacility, request.getIdFacility());
    }

    @Test
    void testDeleteShift() {
        String idFacilityShift = "123";
        when(afFacilityShiftService.deleteShift(idFacilityShift))
                .thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.delete(idFacilityShift);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testChangeStatus() {
        String idFacilityShift = "123";
        when(afFacilityShiftService.changeStatus(idFacilityShift))
                .thenReturn((ResponseEntity) ResponseEntity.ok("statusChanged"));
        ResponseEntity<?> res = controller.changeStatus(idFacilityShift);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("statusChanged", res.getBody());
    }
}
