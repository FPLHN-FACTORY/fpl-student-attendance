package udpm.hn.studentattendance.core.teacher.factory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.service.TCPlanDateAttendanceService;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TCPlanDateAttendanceRestControllerTest {
    @Mock
    private TCPlanDateAttendanceService tcPlanDateAttendanceService;
    
    @InjectMocks
    private TCPlanDateAttendanceRestController tcPlanDateAttendanceRestController;

    @Test
    void testTCPlanDateAttendanceRestControllerExists() {
        assertNotNull(tcPlanDateAttendanceRestController);
    }

    @Test
    void testGetDetail() {
        String idPlanDate = "123";
        when(tcPlanDateAttendanceService.getDetail(idPlanDate)).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = tcPlanDateAttendanceRestController.getDetail(idPlanDate);
        
        assertNotNull(response);
        verify(tcPlanDateAttendanceService).getDetail(idPlanDate);
    }

    @Test
    void testGetAllList() {
        String idPlanDate = "123";
        TCFilterPlanDateAttendanceRequest request = new TCFilterPlanDateAttendanceRequest();
        when(tcPlanDateAttendanceService.getAllList(request)).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = tcPlanDateAttendanceRestController.getAllList(idPlanDate, request);
        
        assertNotNull(response);
        verify(tcPlanDateAttendanceService).getAllList(request);
    }
} 