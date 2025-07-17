package udpm.hn.studentattendance.core.admin.statistics.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.statistics.service.ADStatisticsService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ADStatisticsRestControllerTest {
    @Mock
    private ADStatisticsService adStatisticsService;

    @InjectMocks
    private ADStatisticsRestController adStatisticsRestController;

    @Test
    void testADStatisticsRestControllerExists() {
        assertNotNull(adStatisticsRestController);
    }

    @Test
    void testGetAllListStatistics() {
        when(adStatisticsService.getAllListStats()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adStatisticsRestController.getAllListStatistics();
        assertNotNull(response);
        verify(adStatisticsService).getAllListStats();
    }

    @Test
    void testGetAttendancePercentage() {
        int year = 2024;
        when(adStatisticsService.getLineChartStats(year)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adStatisticsRestController.getAttendancePercentage(year);
        assertNotNull(response);
        verify(adStatisticsService).getLineChartStats(year);
    }
}