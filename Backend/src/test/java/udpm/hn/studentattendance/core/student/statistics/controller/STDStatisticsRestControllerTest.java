package udpm.hn.studentattendance.core.student.statistics.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.statistics.service.STDStatisticsService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class STDStatisticsRestControllerTest {
    @Mock
    private STDStatisticsService stdStatisticsService;

    @InjectMocks
    private STDStatisticsRestController stdStatisticsRestController;

    @Test
    void testSTDStatisticsRestControllerExists() {
        assertNotNull(stdStatisticsRestController);
    }

    @Test
    void testGetStatistics() {
        String idSemester = "2024-1";
        when(stdStatisticsService.getStatistics(idSemester)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdStatisticsRestController.getStatistics(idSemester);
        assertNotNull(response);
        verify(stdStatisticsService).getStatistics(idSemester);
    }
}