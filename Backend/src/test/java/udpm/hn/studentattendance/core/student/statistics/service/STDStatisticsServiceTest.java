package udpm.hn.studentattendance.core.student.statistics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class STDStatisticsServiceTest {
    @InjectMocks
    private STDStatisticsService stdStatisticsService;

    @Test
    void testSTDStatisticsServiceExists() {
        assertNotNull(stdStatisticsService);
    }

    @Test
    void testGetStatistics() {
        String idSemester = "2024-1";
        ResponseEntity<?> response = stdStatisticsService.getStatistics(idSemester);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}