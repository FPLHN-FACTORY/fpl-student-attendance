package udpm.hn.studentattendance.core.student.historyattendance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.historyattendance.service.STDHistoryAttendanceService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class STDHistoryAttendanceRestControllerTest {
    @Mock
    private STDHistoryAttendanceService stdHistoryAttendanceService;

    @InjectMocks
    private STDHistoryAttendanceRestController stdHistoryAttendanceRestController;

    @Test
    void testSTDHistoryAttendanceRestControllerExists() {
        assertNotNull(stdHistoryAttendanceRestController);
    }

    @Test
    void testGetAllAttendanceHistory() {
        udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest request = mock(
                udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest.class);
        when(stdHistoryAttendanceService.getAllHistoryAttendanceByStudent(request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceRestController.getAllAttendanceHistory(request);
        assertNotNull(response);
        verify(stdHistoryAttendanceService).getAllHistoryAttendanceByStudent(request);
    }

    @Test
    void testGetAllSemester() {
        when(stdHistoryAttendanceService.getAllSemester()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceRestController.getAllSemester();
        assertNotNull(response);
        verify(stdHistoryAttendanceService).getAllSemester();
    }

    @Test
    void testGetAllFactory() {
        when(stdHistoryAttendanceService.getAllFactoryByUserStudent()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceRestController.getAllFactory();
        assertNotNull(response);
        verify(stdHistoryAttendanceService).getAllFactoryByUserStudent();
    }

    @Test
    void testGetDetailPlanDate() {
        when(stdHistoryAttendanceService.getDetailPlanDate()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceRestController.getDetailPlanDate();
        assertNotNull(response);
        verify(stdHistoryAttendanceService).getDetailPlanDate();
    }
}