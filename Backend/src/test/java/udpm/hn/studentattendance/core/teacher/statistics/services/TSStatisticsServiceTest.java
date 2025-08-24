package udpm.hn.studentattendance.core.teacher.statistics.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TSStatisticsServiceTest {
    @org.mockito.Mock
    private TSStatisticsService tsStatisticsService;

    @Test
    void testTSStatisticsServiceExists() {
        assertNotNull(tsStatisticsService);
    }

    @Test
    void testGetAllStats() {
        String idSemester = "123";
        when(tsStatisticsService.getAllStats(idSemester)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tsStatisticsService.getAllStats(idSemester);
        assertNotNull(response);
    }

    @Test
    void testGetListStatsFactory() {
        TSFilterFactoryStatsRequest request = new TSFilterFactoryStatsRequest();
        when(tsStatisticsService.getListStatsFactory(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tsStatisticsService.getListStatsFactory(request);
        assertNotNull(response);
    }

    @Test
    void testGetListUser() {
        String idSemester = "123";
        when(tsStatisticsService.getListUser(idSemester)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tsStatisticsService.getListUser(idSemester);
        assertNotNull(response);
    }

    @Test
    void testSendMailStats() {
        TSSendMailStatsRequest request = new TSSendMailStatsRequest();
        when(tsStatisticsService.sendMailStats(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tsStatisticsService.sendMailStats(request);
        assertNotNull(response);
    }
}
