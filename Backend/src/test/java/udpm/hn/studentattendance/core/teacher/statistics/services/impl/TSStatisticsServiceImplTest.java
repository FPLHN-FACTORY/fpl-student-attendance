package udpm.hn.studentattendance.core.teacher.statistics.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.teacher.statistics.model.dto.TSAllStatsAndChartDto;
import udpm.hn.studentattendance.core.teacher.statistics.model.dto.TSListUserDto;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSAllStatsResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartLevelProjectResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartSubjectFacilityResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSFactoryStatsResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSUserResponse;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSFactoryRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSLevelProjectRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSSemesterRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSSubjectFacilityRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSUserAdminRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSUserStaffRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TSStatisticsServiceImplTest {

    @Mock
    private TSSemesterRepository semesterRepository;

    @Mock
    private TSLevelProjectRepository levelProjectRepository;

    @Mock
    private TSSubjectFacilityRepository subjectFacilityRepository;

    @Mock
    private TSFactoryRepository factoryRepository;

    @Mock
    private TSUserAdminRepository userAdminRepository;

    @Mock
    private TSUserStaffRepository userStaffRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private MailerHelper mailerHelper;

    @InjectMocks
    private TSStatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(statisticsService, "appName", "Student Attendance");
    }

    @Test
    @DisplayName("getAllStats should return statistics and chart data")
    void testGetAllStats_Success() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        String userId = "teacher-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);

        TSAllStatsResponse mockStats = mock(TSAllStatsResponse.class);
        when(semesterRepository.getAllStats(semesterId, facilityId, userId))
                .thenReturn(Optional.of(mockStats));

        List<TSChartLevelProjectResponse> mockLevelStats = Arrays.asList(
                mock(TSChartLevelProjectResponse.class),
                mock(TSChartLevelProjectResponse.class));
        when(levelProjectRepository.getStats(semesterId, facilityId, userId))
                .thenReturn(mockLevelStats);

        List<TSChartSubjectFacilityResponse> mockSubjectStats = Arrays.asList(
                mock(TSChartSubjectFacilityResponse.class));
        when(subjectFacilityRepository.getStats(semesterId, facilityId, userId))
                .thenReturn(mockSubjectStats);

        // Act
        ResponseEntity<?> response = statisticsService.getAllStats(semesterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy dữ liệu thành công", apiResponse.getMessage());

        TSAllStatsAndChartDto data = (TSAllStatsAndChartDto) apiResponse.getData();
        assertNotNull(data);
        assertEquals(mockStats, data.getStats());
        assertEquals(mockLevelStats, data.getLevelStats());
        assertEquals(mockSubjectStats, data.getSubjectFacilityStats());

        verify(semesterRepository).getAllStats(semesterId, facilityId, userId);
        verify(levelProjectRepository).getStats(semesterId, facilityId, userId);
        verify(subjectFacilityRepository).getStats(semesterId, facilityId, userId);
    }

    @Test
    @DisplayName("getAllStats should return error when stats not found")
    void testGetAllStats_NotFound() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        String userId = "teacher-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);

        when(semesterRepository.getAllStats(semesterId, facilityId, userId))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = statisticsService.getAllStats(semesterId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không thể lấy dữ liệu thống kê", apiResponse.getMessage());

        verify(levelProjectRepository, never()).getStats(anyString(), anyString(), anyString());
        verify(subjectFacilityRepository, never()).getStats(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getListStatsFactory should return paginated factory stats")
    void testGetListStatsFactory() {
        // Arrange
        String facilityId = "facility-1";
        String userId = "teacher-1";
        TSFilterFactoryStatsRequest request = new TSFilterFactoryStatsRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);

        Page<TSFactoryStatsResponse> page = new PageImpl<>(new ArrayList<>());
        when(factoryRepository.getAllByFilter(any(Pageable.class), eq(request)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = statisticsService.getListStatsFactory(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        assertEquals(facilityId, request.getIdFacility());
        assertEquals(userId, request.getIdUserStaff());
        verify(factoryRepository).getAllByFilter(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("getListUser should return admin and staff user lists")
    void testGetListUser() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        String userEmail = "teacher@example.com";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(userEmail);

        List<TSUserResponse> adminUsers = Arrays.asList(
                mock(TSUserResponse.class),
                mock(TSUserResponse.class));
        when(userAdminRepository.getAllList(userEmail)).thenReturn(adminUsers);

        List<TSUserResponse> staffUsers = Arrays.asList(
                mock(TSUserResponse.class));
        when(userStaffRepository.getAllListStaff(facilityId, userEmail)).thenReturn(staffUsers);

        // Act
        ResponseEntity<?> response = statisticsService.getListUser(semesterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy dữ liệu thành công", apiResponse.getMessage());

        TSListUserDto data = (TSListUserDto) apiResponse.getData();
        assertNotNull(data);
        assertEquals(adminUsers, data.getAdmin());
        assertEquals(staffUsers, data.getStaff());

        verify(userAdminRepository).getAllList(userEmail);
        verify(userStaffRepository).getAllListStaff(facilityId, userEmail);
    }

    @Test
    @DisplayName("sendMailStats should send statistics email")
    void testSendMailStats_Success() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "facility-1";
        String userId = "teacher-1";
        String userEmail = "teacher@example.com";
        String userName = "Teacher Name";
        String userCode = "GV001";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserEmail()).thenReturn(userEmail);
        when(sessionHelper.getUserName()).thenReturn(userName);
        when(sessionHelper.getUserCode()).thenReturn(userCode);

        TSSendMailStatsRequest request = new TSSendMailStatsRequest();
        request.setIdSemester(semesterId);

        // Set date range (today and tomorrow)
        long today = System.currentTimeMillis();
        long tomorrow = today + 86400000;
        request.setRangeDate(Arrays.asList(today, tomorrow));

        // Mock email lists
        Set<String> adminEmails = new HashSet<>(Arrays.asList("admin1@example.com", "admin2@example.com"));
        Set<String> staffEmails = new HashSet<>(Arrays.asList("staff1@example.com"));
        request.setEmailAdmin(adminEmails);
        request.setEmailStaff(staffEmails);

        // Mock semester
        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn(semesterId);
        when(semester.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(semester.getFromDate()).thenReturn(today - 86400000); // Yesterday
        when(semester.getToDate()).thenReturn(tomorrow + 86400000); // Day after tomorrow
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));

        // Mock facility
        Facility facility = mock(Facility.class);
        when(facility.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        // Mock email sending
        doNothing().when(mailerHelper).send(any(MailerDefaultRequest.class));

        // Act
        ResponseEntity<?> response = statisticsService.sendMailStats(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Gửi báo cáo thống kê thành công", apiResponse.getMessage());

        verify(mailerHelper).send(any(MailerDefaultRequest.class));
    }

    @Test
    @DisplayName("sendMailStats should return error when semester not found")
    void testSendMailStats_SemesterNotFound() {
        // Arrange
        String semesterId = "non-existent-semester";

        TSSendMailStatsRequest request = new TSSendMailStatsRequest();
        request.setIdSemester(semesterId);

        when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = statisticsService.sendMailStats(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy học kỳ", apiResponse.getMessage());

        verify(mailerHelper, never()).send(any(MailerDefaultRequest.class));
    }

    @Test
    @DisplayName("sendMailStats should return error when facility not found")
    void testSendMailStats_FacilityNotFound() {
        // Arrange
        String semesterId = "semester-1";
        String facilityId = "non-existent-facility";

        TSSendMailStatsRequest request = new TSSendMailStatsRequest();
        request.setIdSemester(semesterId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        // Mock semester
        Semester semester = mock(Semester.class);
        when(semester.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));

        // Facility not found
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = statisticsService.sendMailStats(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cơ sở không tồn tại hoặc đã ngừng hoạt động", apiResponse.getMessage());

        verify(mailerHelper, never()).send(any(MailerDefaultRequest.class));
    }
}