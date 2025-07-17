package udpm.hn.studentattendance.core.admin.statistics.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSSubjectFacilityChartResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSProjectSubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADStatisticsRepository;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADStatisticsServiceImplTest {

    @Mock
    private ADSSubjectFacilityExtendRepository subjectFacilityExtendRepository;

    @Mock
    private ADStatisticsRepository statisticsRepository;

    @Mock
    private ADSProjectSubjectFacilityRepository projectSubjectFacilityRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private ADStatisticsServiceImpl adStatisticsService;

    @BeforeEach
    void setUp() {
        // No need to set redisTTL as Redis caching is no longer used in the service
    }

    @Test
    @DisplayName("Test getCachedStatistics should return null when statistics data is not available")
    void testGetCachedStatisticsWithNoData() {
        // Given
        when(statisticsRepository.getAllStatistics()).thenReturn(Optional.empty());

        // When
        ADSAllStartsAndChartDTO result = adStatisticsService.getCachedStatistics();

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Test getCachedStatistics should return null when total project and subject data is not available")
    void testGetCachedStatisticsWithNoProjectData() {
        // Given
        ADStatisticsStatResponse statResponse = mock(ADStatisticsStatResponse.class);
        when(statisticsRepository.getAllStatistics()).thenReturn(Optional.of(statResponse));
        when(statisticsRepository.getTotalProjectAndSubject()).thenReturn(Optional.empty());

        // When
        ADSAllStartsAndChartDTO result = adStatisticsService.getCachedStatistics();

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Test getCachedStatistics should return data correctly when all data is available")
    void testGetCachedStatisticsWithAllData() {
        // Given
        ADStatisticsStatResponse statResponse = mock(ADStatisticsStatResponse.class);
        ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse = mock(
                ADSTotalProjectAndSubjectResponse.class);

        List<ADSSubjectFacilityChartResponse> subjectFacilityChartResponses = new ArrayList<>();
        ADSSubjectFacilityChartResponse chartResponse = mock(ADSSubjectFacilityChartResponse.class);
        subjectFacilityChartResponses.add(chartResponse);

        when(statisticsRepository.getAllStatistics()).thenReturn(Optional.of(statResponse));
        when(statisticsRepository.getTotalProjectAndSubject()).thenReturn(Optional.of(totalProjectAndSubjectResponse));
        when(subjectFacilityExtendRepository.getSubjectByFacility()).thenReturn(subjectFacilityChartResponses);

        // When
        ADSAllStartsAndChartDTO result = adStatisticsService.getCachedStatistics();

        // Then
        assertNotNull(result);
        assertEquals(statResponse, result.getStatisticsStatResponse());
        assertEquals(subjectFacilityChartResponses, result.getSubjectFacilityChartResponse());
        assertEquals(totalProjectAndSubjectResponse, result.getTotalProjectAndSubjectResponse());
    }

    @Test
    @DisplayName("Test getAllListStats should return error when statistics data is not available")
    void testGetAllListStatsWithNoData() {
        // Mock the getCachedStatistics method to return null
        ADStatisticsServiceImpl spyService = spy(adStatisticsService);
        doReturn(null).when(spyService).getCachedStatistics();

        // When
        ResponseEntity<?> response = spyService.getAllListStats();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không thể lấy dữ liệu thống kê", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getAllListStats should return data successfully")
    void testGetAllListStatsSuccess() {
        // Given
        ADSAllStartsAndChartDTO cachedData = new ADSAllStartsAndChartDTO();
        ADStatisticsStatResponse statResponse = mock(ADStatisticsStatResponse.class);
        cachedData.setStatisticsStatResponse(statResponse);

        // Mock the getCachedStatistics method
        ADStatisticsServiceImpl spyService = spy(adStatisticsService);
        doReturn(cachedData).when(spyService).getCachedStatistics();

        // When
        ResponseEntity<?> response = spyService.getAllListStats();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thống kê thành công", apiResponse.getMessage());
        assertEquals(cachedData, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getLineChartStats should return chart data correctly for specified year")
    void testGetLineChartStats() {
        // Given
        int testYear = 2023;
        List<ADSProjectSubjectFacilityResponse> mockResponses = new ArrayList<>();
        ADSProjectSubjectFacilityResponse response1 = mock(ADSProjectSubjectFacilityResponse.class);
        ADSProjectSubjectFacilityResponse response2 = mock(ADSProjectSubjectFacilityResponse.class);
        mockResponses.add(response1);
        mockResponses.add(response2);

        when(projectSubjectFacilityRepository.getProjectSubjectFacilityResponses(testYear)).thenReturn(mockResponses);

        // When
        ResponseEntity<?> result = adStatisticsService.getLineChartStats(testYear);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals(" Lấy thống kê năm " + testYear + " thành công", apiResponse.getMessage());
        assertEquals(mockResponses, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getLineChartStats should return empty list when no data for specified year")
    void testGetLineChartStatsWithEmptyData() {
        // Given
        int testYear = 2024;
        List<ADSProjectSubjectFacilityResponse> emptyList = new ArrayList<>();

        when(projectSubjectFacilityRepository.getProjectSubjectFacilityResponses(testYear)).thenReturn(emptyList);

        // When
        ResponseEntity<?> result = adStatisticsService.getLineChartStats(testYear);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals(" Lấy thống kê năm " + testYear + " thành công", apiResponse.getMessage());
        assertEquals(emptyList, apiResponse.getData());
    }
}