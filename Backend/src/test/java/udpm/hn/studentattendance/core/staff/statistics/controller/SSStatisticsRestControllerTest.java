package udpm.hn.studentattendance.core.staff.statistics.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.statistics.services.SSStatisticsService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SSStatisticsRestControllerTest {
    @Mock
    private SSStatisticsService ssStatisticsService;

    @InjectMocks
    private SSStatisticsRestController ssStatisticsRestController;

    @Test
    void testSSStatisticsRestControllerExists() {
        assertNotNull(ssStatisticsRestController);
    }

    @Test
    void testGetAllStats() {
        String idSemester = "2024-1";
        when(ssStatisticsService.getAllStats(idSemester)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = ssStatisticsRestController.getAllStats(idSemester);
        assertNotNull(response);
        verify(ssStatisticsService).getAllStats(idSemester);
    }

    @Test
    void testGetListStatsFactory() {
        String idSemester = "2024-1";
        udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest request = mock(
                udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest.class);
        when(ssStatisticsService.getListStatsFactory(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = ssStatisticsRestController.getListStatsFactory(idSemester, request);
        assertNotNull(response);
        verify(ssStatisticsService).getListStatsFactory(request);
    }

    @Test
    void testGetListUser() {
        String idSemester = "2024-1";
        when(ssStatisticsService.getListUser(idSemester)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = ssStatisticsRestController.getListUser(idSemester);
        assertNotNull(response);
        verify(ssStatisticsService).getListUser(idSemester);
    }

    @Test
    void testSendMailStats() {
        String idSemester = "2024-1";
        udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest request = mock(
                udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest.class);
        when(ssStatisticsService.sendMailStats(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = ssStatisticsRestController.sendMailStats(idSemester, request);
        assertNotNull(response);
        verify(ssStatisticsService).sendMailStats(request);
    }
}
