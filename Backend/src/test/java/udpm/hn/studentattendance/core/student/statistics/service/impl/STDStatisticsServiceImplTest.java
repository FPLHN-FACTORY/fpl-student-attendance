package udpm.hn.studentattendance.core.student.statistics.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.statistics.model.dto.STDStatisticDto;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryAttendanceBarChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryLineChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDStatisticsSemesterRepository;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STDStatisticsServiceImplTest {

    @Mock
    private STDStatisticsSemesterRepository stdStatisticsSemesterRepository;

    @Mock
    private STDSFactoryLineChartRepository factoryLineChartRepository;

    @Mock
    private STDSFactoryAttendanceBarChartRepository attendanceBarChartRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private STDStatisticsStatResponse mockStatResponse;

    @InjectMocks
    private STDStatisticsServiceImpl stdStatisticsService;

    @Test
    @DisplayName("Test getStatistics should return complete statistics data")
    void testGetStatistics() {
        // Given
        String semesterId = "semester-123";
        String facilityId = "facility-123";
        String userId = "user-123";

        // Mock responses
        STDStatisticsStatResponse statResponse = mock(STDStatisticsStatResponse.class);
        when(statResponse.getFactory()).thenReturn(5);
        when(statResponse.getPass()).thenReturn(3);
        when(statResponse.getFail()).thenReturn(1);
        when(statResponse.getProcess()).thenReturn(1);
        when(statResponse.getNotStarted()).thenReturn(0);
        when(statResponse.getComplete()).thenReturn(4);

        List<STDStatisticsFactoryChartResponse> factoryChartResponses = new ArrayList<>();
        STDStatisticsFactoryChartResponse factoryChartResponse = mock(STDStatisticsFactoryChartResponse.class);
        when(factoryChartResponse.getFactoryName()).thenReturn("Factory 1");
        when(factoryChartResponse.getAttendancePercentage()).thenReturn(85.5f);
        factoryChartResponses.add(factoryChartResponse);

        List<STDStatisticsAttendanceChartResponse> attendanceChartResponses = new ArrayList<>();
        STDStatisticsAttendanceChartResponse attendanceChartResponse = mock(STDStatisticsAttendanceChartResponse.class);
        when(attendanceChartResponse.getFactoryName()).thenReturn("Factory 1");
        when(attendanceChartResponse.getTotalShift()).thenReturn(20);
        when(attendanceChartResponse.getTotalAbsent()).thenReturn(3);
        attendanceChartResponses.add(attendanceChartResponse);

        // Setup mocks
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                .thenReturn(Optional.of(statResponse));
        when(factoryLineChartRepository.getAttendancePercentage(facilityId, userId, semesterId))
                .thenReturn(factoryChartResponses);
        when(attendanceBarChartRepository.getAttendanceBarChart(userId, semesterId))
                .thenReturn(attendanceChartResponses);

        // When
        ResponseEntity<?> response = stdStatisticsService.getStatistics(semesterId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thống kê thành công", apiResponse.getMessage());

        STDStatisticDto result = (STDStatisticDto) apiResponse.getData();
        assertNotNull(result);
        assertEquals(statResponse, result.getStdStatisticsStatResponse());
        assertEquals(factoryChartResponses, result.getFactoryChartResponse());
        assertEquals(attendanceChartResponses, result.getAttendanceChartResponses());

        // Verify repository calls
        verify(stdStatisticsSemesterRepository).getAllStatisticBySemester(facilityId, userId, semesterId);
        verify(factoryLineChartRepository).getAttendancePercentage(facilityId, userId, semesterId);
        verify(attendanceBarChartRepository).getAttendanceBarChart(userId, semesterId);
    }

    @Test
    @DisplayName("Test getStatistics when no statistics response is available")
    void testGetStatisticsNoStatisticsFound() {
        // Given
        String semesterId = "semester-123";
        String facilityId = "facility-123";
        String userId = "user-123";

        // Empty response from repository
        List<STDStatisticsFactoryChartResponse> factoryChartResponses = new ArrayList<>();
        List<STDStatisticsAttendanceChartResponse> attendanceChartResponses = new ArrayList<>();

        // Setup mocks
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                .thenReturn(Optional.empty());
        when(factoryLineChartRepository.getAttendancePercentage(facilityId, userId, semesterId))
                .thenReturn(factoryChartResponses);
        when(attendanceBarChartRepository.getAttendanceBarChart(userId, semesterId))
                .thenReturn(attendanceChartResponses);

        // When
        ResponseEntity<?> response = stdStatisticsService.getStatistics(semesterId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        STDStatisticDto result = (STDStatisticDto) apiResponse.getData();
        assertNotNull(result);
        assertNull(result.getStdStatisticsStatResponse());
        assertTrue(result.getFactoryChartResponse().isEmpty());
        assertTrue(result.getAttendanceChartResponses().isEmpty());
    }

    @Test
    @DisplayName("Test getStatistics with null semester ID")
    void testGetStatisticsNullSemesterId() {
        // Given
        String semesterId = null;
        String facilityId = "facility-123";
        String userId = "user-123";

        // Setup mocks
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                .thenReturn(Optional.empty());
        when(factoryLineChartRepository.getAttendancePercentage(facilityId, userId, semesterId))
                .thenReturn(new ArrayList<>());
        when(attendanceBarChartRepository.getAttendanceBarChart(userId, semesterId))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<?> response = stdStatisticsService.getStatistics(semesterId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);

        STDStatisticDto result = (STDStatisticDto) apiResponse.getData();
        assertNotNull(result);
    }
}