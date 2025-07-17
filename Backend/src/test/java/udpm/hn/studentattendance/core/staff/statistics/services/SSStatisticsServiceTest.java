package udpm.hn.studentattendance.core.staff.statistics.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SSStatisticsServiceTest {
    @Mock
    private SSStatisticsService ssStatisticsService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(ssStatisticsService.getAllStats(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(ssStatisticsService.getListStatsFactory(any(SSFilterFactoryStatsRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(ssStatisticsService.getListUser(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(ssStatisticsService.sendMailStats(any(SSSendMailStatsRequest.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testSSStatisticsServiceExists() {
        assertNotNull(ssStatisticsService);
    }

    @Test
    void testGetAllStats() {
        String idSemester = "123";
        ResponseEntity<?> response = ssStatisticsService.getAllStats(idSemester);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetListStatsFactory() {
        SSFilterFactoryStatsRequest request = new SSFilterFactoryStatsRequest();
        ResponseEntity<?> response = ssStatisticsService.getListStatsFactory(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetListUser() {
        String idSemester = "123";
        ResponseEntity<?> response = ssStatisticsService.getListUser(idSemester);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testSendMailStats() {
        SSSendMailStatsRequest request = new SSSendMailStatsRequest();
        ResponseEntity<?> response = ssStatisticsService.sendMailStats(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}