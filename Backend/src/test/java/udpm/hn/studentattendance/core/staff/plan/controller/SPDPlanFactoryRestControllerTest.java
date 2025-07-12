package udpm.hn.studentattendance.core.staff.plan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanFactoryService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SPDPlanFactoryRestControllerTest {
    @Mock
    private SPDPlanFactoryService spdPlanFactoryService;

    @InjectMocks
    private SPDPlanFactoryRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllList() {
        SPDFilterPlanFactoryRequest request = new SPDFilterPlanFactoryRequest();
        when(spdPlanFactoryService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList(request, "1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals("1", request.getIdPlan());
    }

    @Test
    void testCreatePlanFactory() {
        SPDAddPlanFactoryRequest request = new SPDAddPlanFactoryRequest();
        when(spdPlanFactoryService.createPlanFactory(request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.createPlanFactory(request, "1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
        assertEquals("1", request.getIdPlan());
    }

    @Test
    void testGetAllFactory() {
        when(spdPlanFactoryService.getListFactory("1")).thenReturn((ResponseEntity) ResponseEntity.ok("factories"));
        ResponseEntity<?> res = controller.getAllFactory("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("factories", res.getBody());
    }

    @Test
    void testGetAllShift() {
        when(spdPlanFactoryService.getListShift()).thenReturn((ResponseEntity) ResponseEntity.ok("shifts"));
        ResponseEntity<?> res = controller.getAllShift();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("shifts", res.getBody());
    }

    @Test
    void testDeletePlan() {
        when(spdPlanFactoryService.deletePlanFactory("1")).thenReturn((ResponseEntity) ResponseEntity.ok("deleted"));
        ResponseEntity<?> res = controller.deletePlan("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("deleted", res.getBody());
    }

    @Test
    void testChangeStatus() {
        when(spdPlanFactoryService.changeStatus("1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeStatus("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }
}