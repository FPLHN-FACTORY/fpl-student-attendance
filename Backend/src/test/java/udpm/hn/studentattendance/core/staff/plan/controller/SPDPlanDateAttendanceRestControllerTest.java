package udpm.hn.studentattendance.core.staff.plan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateAttendanceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SPDPlanDateAttendanceRestControllerTest {
    @Mock
    private SPDPlanDateAttendanceService spdPlanDateAttendanceService;

    @InjectMocks
    private SPDPlanDateAttendanceRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDetail() {
        when(spdPlanDateAttendanceService.getDetail("1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = controller.getDetail("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }

    @Test
    void testGetAllList() {
        SPDFilterPlanDateAttendanceRequest request = new SPDFilterPlanDateAttendanceRequest();
        when(spdPlanDateAttendanceService.getAllList(request)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getAllList("1", request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals("1", request.getIdPlanDate());
    }

    @Test
    void testChangeStatus() {
        SPDModifyPlanDateAttendanceRequest request = new SPDModifyPlanDateAttendanceRequest();
        when(spdPlanDateAttendanceService.changeStatus(request)).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeStatus(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }
} 
