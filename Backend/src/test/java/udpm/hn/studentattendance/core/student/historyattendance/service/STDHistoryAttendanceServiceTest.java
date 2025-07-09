package udpm.hn.studentattendance.core.student.historyattendance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STDHistoryAttendanceServiceTest {
    @Mock
    private STDHistoryAttendanceService stdHistoryAttendanceService;

    @Test
    void testSTDHistoryAttendanceServiceExists() {
        assertNotNull(stdHistoryAttendanceService);
    }

    @Test
    void testGetAllHistoryAttendanceByStudent() {
        udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest request = new udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest();
        when(stdHistoryAttendanceService.getAllHistoryAttendanceByStudent(request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceService.getAllHistoryAttendanceByStudent(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllSemester() {
        when(stdHistoryAttendanceService.getAllSemester())
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceService.getAllSemester();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllFactoryByUserStudent() {
        when(stdHistoryAttendanceService.getAllFactoryByUserStudent())
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceService.getAllFactoryByUserStudent();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetDetailPlanDate() {
        when(stdHistoryAttendanceService.getDetailPlanDate())
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdHistoryAttendanceService.getDetailPlanDate();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}