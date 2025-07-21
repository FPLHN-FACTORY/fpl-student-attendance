package udpm.hn.studentattendance.core.student.statistics.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.student.statistics.model.dto.STDStatisticDto;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryAttendanceBarChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryLineChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDStatisticsSemesterRepository;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        private RedisService redisService;

        @Mock
        private RedisInvalidationHelper redisInvalidationHelper;

        @Mock
        private STDStatisticsStatResponse mockStatResponse;

        @Spy
        @InjectMocks
        private STDStatisticsServiceImpl stdStatisticsService;

        @BeforeEach
        void setUp() {
                // No setup needed for this service
        }

        @Test
        @DisplayName("Test getStatistics should return complete statistics data")
        void testGetStatistics_Success() {
                // Given
                String semesterId = "semester-123";
                String facilityId = "facility-123";
                String userId = "user-123";

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(redisService.get(anyString())).thenReturn(null);

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

                STDStatisticsAttendanceChartResponse attendanceChartResponses = mock(
                                STDStatisticsAttendanceChartResponse.class);
                STDStatisticsAttendanceChartResponse attendanceChartResponse = mock(
                                STDStatisticsAttendanceChartResponse.class);
                when(attendanceChartResponse.getTotalShift()).thenReturn(20);
                when(attendanceChartResponse.getTotalAbsent()).thenReturn(3);

                when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                                .thenReturn(Optional.of(statResponse));
                when(factoryLineChartRepository.getAttendancePercentage(facilityId, userId, semesterId))
                                .thenReturn(factoryChartResponses);
                when(attendanceBarChartRepository.getAttendanceBarChart(userId, semesterId))
                                .thenReturn(Optional.of(attendanceChartResponses));

                // Act
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

                verify(stdStatisticsSemesterRepository).getAllStatisticBySemester(facilityId, userId, semesterId);
                verify(factoryLineChartRepository).getAttendancePercentage(facilityId, userId, semesterId);
                verify(attendanceBarChartRepository).getAttendanceBarChart(userId, semesterId);
        }

        @Test
        @DisplayName("Test getStatistics when no statistics response is available")
        void testGetStatistics_NotFound() {
                // Given
                String semesterId = "semester-123";
                String facilityId = "facility-123";
                String userId = "user-123";

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(redisService.get(anyString())).thenReturn(null);

                when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                                .thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = stdStatisticsService.getStatistics(semesterId);

                // Then
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy dữ liệu thống kê thành công", apiResponse.getMessage());

                STDStatisticDto result = (STDStatisticDto) apiResponse.getData();
                assertNotNull(result);
                assertNull(result.getStdStatisticsStatResponse());
                assertNotNull(result.getFactoryChartResponse());
                assertNull(result.getAttendanceChartResponses());

                verify(stdStatisticsSemesterRepository).getAllStatisticBySemester(facilityId, userId, semesterId);
                verify(factoryLineChartRepository).getAttendancePercentage(facilityId, userId, semesterId);
                verify(attendanceBarChartRepository).getAttendanceBarChart(userId, semesterId);
        }

        @Test
        @DisplayName("Test getStatistics with null semester ID")
        void testGetStatistics_NullSemesterId() {
                // Given
                String semesterId = null;
                String facilityId = "facility-123";
                String userId = "user-123";

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(redisService.get(anyString())).thenReturn(null);

                when(stdStatisticsSemesterRepository.getAllStatisticBySemester(facilityId, userId, semesterId))
                                .thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = stdStatisticsService.getStatistics(semesterId);

                // Then
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy dữ liệu thống kê thành công", apiResponse.getMessage());

                STDStatisticDto result = (STDStatisticDto) apiResponse.getData();
                assertNotNull(result);
                assertNull(result.getStdStatisticsStatResponse());
                assertNotNull(result.getFactoryChartResponse());
                assertNull(result.getAttendanceChartResponses());

                verify(stdStatisticsSemesterRepository).getAllStatisticBySemester(facilityId, userId, semesterId);
                verify(factoryLineChartRepository).getAttendancePercentage(facilityId, userId, semesterId);
                verify(attendanceBarChartRepository).getAttendanceBarChart(userId, semesterId);
        }
}
