package udpm.hn.studentattendance.core.admin.statistics.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.admin.statistics.model.dto.ADSAllStartsAndChartDTO;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSSubjectFacilityChartResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSProjectSubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADStatisticsRepository;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @InjectMocks
    private ADStatisticsServiceImpl adStatisticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adStatisticsService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("Test getAllListStats should return cached data if available")
    void testGetAllListStatsFromCache() {
        // Given
        ADStatisticRequest request = new ADStatisticRequest();
        int pageNumber = 0;
        String cacheKey = "admin:statistics:" + request.toString() + ":" + pageNumber;

        ADSAllStartsAndChartDTO cachedData = new ADSAllStartsAndChartDTO();

        when(redisService.get(cacheKey)).thenReturn(cachedData);

        // When
        ResponseEntity<?> response = adStatisticsService.getAllListStats(request, pageNumber);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thống kê thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedData, apiResponse.getData());

        // Verify that repositories were not called
        verifyNoInteractions(statisticsRepository);
        verifyNoInteractions(subjectFacilityExtendRepository);
        verifyNoInteractions(projectSubjectFacilityRepository);
    }

    @Test
    @DisplayName("Test getAllListStats should return error when statistics data is not available")
    void testGetAllListStatsWithNoStatisticsData() {
        // Given
        ADStatisticRequest request = new ADStatisticRequest();
        int pageNumber = 0;
        String cacheKey = "admin:statistics:" + request.toString() + ":" + pageNumber;

        when(redisService.get(cacheKey)).thenReturn(null);
        when(statisticsRepository.getAllStatistics(request)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = adStatisticsService.getAllListStats(request, pageNumber);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không thể lấy dữ liệu thống kê", apiResponse.getMessage());

        // Verify repository calls
        verify(statisticsRepository).getAllStatistics(request);
        verifyNoInteractions(subjectFacilityExtendRepository);
        verifyNoInteractions(projectSubjectFacilityRepository);
    }

    @Test
    @DisplayName("Test getAllListStats should fetch and cache data if not in cache")
    void testGetAllListStatsFromRepository() {
        // Given
        ADStatisticRequest request = new ADStatisticRequest();
        int pageNumber = 0;
        String cacheKey = "admin:statistics:" + request.toString() + ":" + pageNumber;

        ADStatisticsStatResponse statResponse = mock(ADStatisticsStatResponse.class);
        when(statResponse.getTotalProject()).thenReturn(10);
        when(statResponse.getSubject()).thenReturn(5);

        ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse = mock(
                ADSTotalProjectAndSubjectResponse.class);
        when(totalProjectAndSubjectResponse.getTotalProject()).thenReturn(8);
        when(totalProjectAndSubjectResponse.getTotalSubject()).thenReturn(2);

        List<ADSSubjectFacilityChartResponse> subjectFacilityChartResponses = new ArrayList<>();
        ADSSubjectFacilityChartResponse chartResponse = mock(ADSSubjectFacilityChartResponse.class);
        subjectFacilityChartResponses.add(chartResponse);

        List<ADSProjectSubjectFacilityResponse> projectResponses = new ArrayList<>();
        ADSProjectSubjectFacilityResponse projectResponse = mock(ADSProjectSubjectFacilityResponse.class);
        projectResponses.add(projectResponse);
        Page<ADSProjectSubjectFacilityResponse> projectPage = new PageImpl<>(projectResponses);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(statisticsRepository.getAllStatistics(request)).thenReturn(Optional.of(statResponse));
        when(statisticsRepository.getTotalProjectAndSubject()).thenReturn(Optional.of(totalProjectAndSubjectResponse));
        when(subjectFacilityExtendRepository.getSubjectByFacility()).thenReturn(subjectFacilityChartResponses);
        when(projectSubjectFacilityRepository.getProjectSubjectFacilityResponses(any(PageRequest.class)))
                .thenReturn(projectPage);

        // When
        ResponseEntity<?> response = adStatisticsService.getAllListStats(request, pageNumber);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thống kê thành công", apiResponse.getMessage());

        ADSAllStartsAndChartDTO result = (ADSAllStartsAndChartDTO) apiResponse.getData();
        assertNotNull(result);
        assertEquals(statResponse, result.getStatisticsStatResponse());
        assertEquals(subjectFacilityChartResponses, result.getSubjectFacilityChartResponse());
        assertEquals(projectPage, result.getProjectSubjectFacilityResponses());
        assertEquals(totalProjectAndSubjectResponse, result.getTotalProjectAndSubjectResponse());

        // Verify repository and cache calls
        verify(statisticsRepository).getAllStatistics(request);
        verify(statisticsRepository).getTotalProjectAndSubject();
        verify(subjectFacilityExtendRepository).getSubjectByFacility();
        verify(projectSubjectFacilityRepository).getProjectSubjectFacilityResponses(any(PageRequest.class));
        verify(redisService).set(eq(cacheKey), any(ADSAllStartsAndChartDTO.class), eq(3600L * 3));
    }

    @Test
    @DisplayName("Test invalidateAdminStatisticsCache should delete cached statistics")
    void testInvalidateAdminStatisticsCache() {
        // When
        adStatisticsService.invalidateAdminStatisticsCache();

        // Then
        verify(redisService).deletePattern("admin:statistics:*");
    }
}