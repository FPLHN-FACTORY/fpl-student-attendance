package udpm.hn.studentattendance.core.admin.statistics.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.statistics.model.dto.ADSAllStartsAndChartDTO;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.service.ADStatisticsService;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADStatisticsRestControllerTest {

    @Mock
    private ADStatisticsService adStatisticsService;

    @InjectMocks
    private ADStatisticsRestController controller;

    @Test
    @DisplayName("Test getAllListStatistics should call service and return its result")
    void testGetAllListStatistics() {
        // Given
        ADSAllStartsAndChartDTO dto = new ADSAllStartsAndChartDTO();
        ApiResponse apiResponse = ApiResponse.success("Lấy dữ liệu thống kê thành công", dto);

        ResponseEntity<?> responseEntity = ResponseEntity.ok(apiResponse);
        doReturn(responseEntity).when(adStatisticsService).getAllListStats();

        // When
        ResponseEntity<?> result = controller.getAllListStatistics();

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
        verify(adStatisticsService).getAllListStats();
    }

    @Test
    @DisplayName("Test getAttendancePercentage should call service with correct year parameter")
    void testGetAttendancePercentage() {
        // Given
        int testYear = 2023;

        List<ADSProjectSubjectFacilityResponse> mockResponses = new ArrayList<>();
        ApiResponse apiResponse = ApiResponse.success(" Lấy thống kê năm " + testYear + " thành công", mockResponses);

        ResponseEntity<?> responseEntity = ResponseEntity.ok(apiResponse);
        doReturn(responseEntity).when(adStatisticsService).getLineChartStats(testYear);

        // When
        ResponseEntity<?> result = controller.getAttendancePercentage(testYear);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
        verify(adStatisticsService).getLineChartStats(testYear);
    }
}