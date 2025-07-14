package udpm.hn.studentattendance.core.staff.plan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDUpdateLinkMeetRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SPDPlanDateRestControllerTest {
    @Mock
    private SPDPlanDateService spdPlanDateService;

    @InjectMocks
    private SPDPlanDateRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDetail() {
        when(spdPlanDateService.getDetail("1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = controller.getDetail("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }

    @Test
    void testGetAllList() {
        SPDFilterPlanDateRequest request = new SPDFilterPlanDateRequest();
        when(spdPlanDateService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList("1", request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals("1", request.getIdPlanFactory());
    }

    @Test
    void testDeleteMultiplePlanDate() {
        SPDDeletePlanDateRequest request = new SPDDeletePlanDateRequest();
        when(spdPlanDateService.deleteMultiplePlanDate(request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deleteMultiplePlanDate(request, "1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testDeletePlanDate() {
        when(spdPlanDateService.deletePlanDate("1")).thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deletePlanDate("1", "factory1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testAddPlanDate() {
        SPDAddOrUpdatePlanDateRequest request = new SPDAddOrUpdatePlanDateRequest();
        when(spdPlanDateService.addPlanDate(request)).thenReturn((ResponseEntity) ResponseEntity.ok("added"));
        ResponseEntity<?> res = controller.addPlanDate("1", request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("added", res.getBody());
        assertEquals("1", request.getIdPlanFactory());
    }

    @Test
    void testUpdatePlanDate() {
        SPDAddOrUpdatePlanDateRequest request = new SPDAddOrUpdatePlanDateRequest();
        when(spdPlanDateService.updatePlanDate(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updatePlanDate(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testUpdateLinkMeet() {
        SPDUpdateLinkMeetRequest request = new SPDUpdateLinkMeetRequest();
        when(spdPlanDateService.updateLinkMeet(request)).thenReturn((ResponseEntity) ResponseEntity.ok("linkUpdated"));
        ResponseEntity<?> res = controller.updateLinkMeet( request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("linkUpdated", res.getBody());
    }

    @Test
    void testSendMail() {
        when(spdPlanDateService.sendMail("1")).thenReturn((ResponseEntity) ResponseEntity.ok("mailSent"));
        ResponseEntity<?> res = controller.sendMail("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("mailSent", res.getBody());
    }
}