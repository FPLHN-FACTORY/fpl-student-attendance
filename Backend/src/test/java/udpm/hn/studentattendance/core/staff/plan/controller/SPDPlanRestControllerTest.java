package udpm.hn.studentattendance.core.staff.plan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SPDPlanRestControllerTest {
    @Mock
    private SPDPlanService spdPlanService;

    @InjectMocks
    private SPDPlanRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllList() {
        SPDFilterPlanRequest request = new SPDFilterPlanRequest();
        when(spdPlanService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
    }

    @Test
    void testGetAllSubject() {
        when(spdPlanService.getAllSubject()).thenReturn((ResponseEntity) ResponseEntity.ok("subjects"));
        ResponseEntity<?> res = controller.getAllSubject();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("subjects", res.getBody());
    }

    @Test
    void testGetAllLevel() {
        when(spdPlanService.getAllLevel()).thenReturn((ResponseEntity) ResponseEntity.ok("levels"));
        ResponseEntity<?> res = controller.getAllLevel();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("levels", res.getBody());
    }

    @Test
    void testGetListSemester() {
        when(spdPlanService.getListSemester()).thenReturn((ResponseEntity) ResponseEntity.ok("semesters"));
        ResponseEntity<?> res = controller.getListSemester();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("semesters", res.getBody());
    }

    @Test
    void testGetAllYear() {
        when(spdPlanService.getAllYear()).thenReturn((ResponseEntity) ResponseEntity.ok("years"));
        ResponseEntity<?> res = controller.getAllYear();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("years", res.getBody());
    }

    @Test
    void testGetAllFactory() {
        SPDFilterCreatePlanRequest request = new SPDFilterCreatePlanRequest();
        when(spdPlanService.getListProject(request)).thenReturn((ResponseEntity) ResponseEntity.ok("projects"));
        ResponseEntity<?> res = controller.getAllFactory(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("projects", res.getBody());
    }

    @Test
    void testGetPlan() {
        when(spdPlanService.getPlan("1")).thenReturn((ResponseEntity) ResponseEntity.ok("plan"));
        ResponseEntity<?> res = controller.getPlan("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("plan", res.getBody());
    }

    @Test
    void testDeletePlan() {
        when(spdPlanService.deletePlan("1")).thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deletePlan("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testChangeStatus() {
        when(spdPlanService.changeStatus("1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeStatus("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }

    @Test
    void testCreatePlan() {
        SPDAddOrUpdatePlanRequest request = new SPDAddOrUpdatePlanRequest();
        when(spdPlanService.createPlan(request)).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.createPlan(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testUpdatePlan() {
        SPDAddOrUpdatePlanRequest request = new SPDAddOrUpdatePlanRequest();
        when(spdPlanService.updatePlan(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updatePlan(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }
}