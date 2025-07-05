package udpm.hn.studentattendance.core.staff.statistics.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.statistics.model.dto.SSAllStatsAndChartDto;
import udpm.hn.studentattendance.core.staff.statistics.model.dto.SSListUserDto;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.response.*;
import udpm.hn.studentattendance.core.staff.statistics.repositories.*;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSStatisticsServiceImplTest {

    @Mock
    private SSSemesterRepository ssSemesterRepository;

    @Mock
    private SSLevelProjectRepository ssLevelProjectRepository;

    @Mock
    private SSSubjectFacilityRepository sSSubjectFacilityRepository;

    @Mock
    private SSFactoryRepository ssFactoryRepository;

    @Mock
    private SSUserAdminRepository ssUserAdminRepository;

    @Mock
    private SSUserStaffRepository ssUserStaffRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private SSStatisticsServiceImpl ssStatisticsService;

    @Test
    @DisplayName("getAllStats should return combined statistics when data exists")
    void testGetAllStats_Success() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        SSAllStatsResponse statsResponse = mock(SSAllStatsResponse.class);
        when(ssSemesterRepository.getAllStats(semesterId, facilityId)).thenReturn(Optional.of(statsResponse));

        List<SSChartLevelProjectResponse> levelChart = List.of(mock(SSChartLevelProjectResponse.class));
        when(ssLevelProjectRepository.getStats(semesterId, facilityId)).thenReturn(levelChart);

        List<SSChartSubjectFacilityResponse> subjectFacilityChart = List.of(mock(SSChartSubjectFacilityResponse.class));
        when(sSSubjectFacilityRepository.getStats(semesterId, facilityId)).thenReturn(subjectFacilityChart);

        // Act
        ResponseEntity<?> response = ssStatisticsService.getAllStats(semesterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thành công", apiResponse.getMessage());

        SSAllStatsAndChartDto dto = (SSAllStatsAndChartDto) apiResponse.getData();
        assertNotNull(dto);
        assertEquals(statsResponse, dto.getStats());
        assertEquals(levelChart, dto.getLevelStats());
        assertEquals(subjectFacilityChart, dto.getSubjectFacilityStats());

        // Verify interactions
        verify(ssSemesterRepository).getAllStats(semesterId, facilityId);
        verify(ssLevelProjectRepository).getStats(semesterId, facilityId);
        verify(sSSubjectFacilityRepository).getStats(semesterId, facilityId);
    }

    @Test
    @DisplayName("getAllStats should return error when stats data is missing")
    void testGetAllStats_NoStats() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(ssSemesterRepository.getAllStats(anyString(), anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = ssStatisticsService.getAllStats(semesterId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không thể lấy dữ liệu thống kê", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getListUser should aggregate users from repositories")
    void testGetListUser() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        String currentEmail = "current@staff.com";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(currentEmail);

        List<SSUserResponse> adminList = List.of(mock(SSUserResponse.class));
        List<SSUserResponse> staffList = List.of(mock(SSUserResponse.class));
        List<SSUserResponse> teacherList = List.of(mock(SSUserResponse.class));
        when(ssUserAdminRepository.getAllList(currentEmail)).thenReturn(adminList);
        when(ssUserStaffRepository.getAllListStaff(facilityId, currentEmail)).thenReturn(staffList);
        when(ssUserStaffRepository.getAllListTeacher(facilityId, semesterId, currentEmail)).thenReturn(teacherList);

        // Act
        ResponseEntity<?> response = ssStatisticsService.getListUser(semesterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        SSListUserDto dto = (SSListUserDto) apiResponse.getData();
        assertNotNull(dto);
        assertEquals(adminList, dto.getAdmin());
        assertEquals(staffList, dto.getStaff());
        assertEquals(teacherList, dto.getTeacher());

        verify(ssUserAdminRepository).getAllList(currentEmail);
        verify(ssUserStaffRepository).getAllListStaff(facilityId, currentEmail);
        verify(ssUserStaffRepository).getAllListTeacher(facilityId, semesterId, currentEmail);
    }

    @Test
    @DisplayName("getListStatsFactory should call repository and return success response")
    void testGetListStatsFactory() {
        // Arrange
        SSFilterFactoryStatsRequest request = new SSFilterFactoryStatsRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        org.springframework.data.domain.Page<SSFactoryStatsResponse> emptyPage = org.springframework.data.domain.Page
                .empty();
        when(ssFactoryRepository.getAllByFilter(any(), any())).thenReturn(emptyPage);

        // Act
        ResponseEntity<?> response = ssStatisticsService.getListStatsFactory(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        verify(ssFactoryRepository).getAllByFilter(any(), any());
    }
}