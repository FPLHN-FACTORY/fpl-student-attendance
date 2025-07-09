package udpm.hn.studentattendance.core.teacher.statistics.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.services.TSStatisticsService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TSStatisticsRestControllerTest {
    @Mock
    private TSStatisticsService tsStatisticsService;

    @InjectMocks
    private TSStatisticsRestController tsStatisticsRestController;

    @Test
    void testTSStatisticsRestControllerExists() {
        assertNotNull(tsStatisticsRestController);
    }

    @Test
    void testGetAllStats() {
        String idSemester = "123";
        when(tsStatisticsService.getAllStats(idSemester)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = tsStatisticsRestController.getAllStats(idSemester);

        assertNotNull(response);
        verify(tsStatisticsService).getAllStats(idSemester);
    }

    @Test
    void testGetListStatsFactory() {
        String idSemester = "123";
        TSFilterFactoryStatsRequest request = new TSFilterFactoryStatsRequest();
        when(tsStatisticsService.getListStatsFactory(request)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = tsStatisticsRestController.getListStatsFactory(idSemester, request);

        assertNotNull(response);
        verify(tsStatisticsService).getListStatsFactory(request);
    }

    @Test
    void testGetListUser() {
        String idSemester = "123";
        when(tsStatisticsService.getListUser(idSemester)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = tsStatisticsRestController.getListUser(idSemester);

        assertNotNull(response);
        verify(tsStatisticsService).getListUser(idSemester);
    }

    @Test
    void testSendMailStats() {
        String idSemester = "123";
        TSSendMailStatsRequest request = new TSSendMailStatsRequest();
        when(tsStatisticsService.sendMailStats(request)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = tsStatisticsRestController.sendMailStats(idSemester, request);

        assertNotNull(response);
        verify(tsStatisticsService).sendMailStats(request);
    }
}