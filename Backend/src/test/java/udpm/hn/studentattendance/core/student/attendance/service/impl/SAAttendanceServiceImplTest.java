package udpm.hn.studentattendance.core.student.attendance.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceResponse;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAAttendanceRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAFacilityIPRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAFacilityLocationRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAPlanDateRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAUserStudentFactoryRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import udpm.hn.studentattendance.utils.AppUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SAAttendanceServiceImplTest {

        @Mock
        private SessionHelper sessionHelper;

        @Mock
        private SAAttendanceRepository attendanceRepository;

        @Mock
        private SAPlanDateRepository planDateRepository;

        @Mock
        private SAUserStudentFactoryRepository userStudentFactoryRepository;

        @Mock
        private SAFacilityIPRepository facilityIPRepository;

        @Mock
        private SAFacilityLocationRepository facilityLocationRepository;

        @Mock
        private HttpServletRequest httpServletRequest;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

        @Mock
        private RedisService redisService;

        @Mock
        private RedisInvalidationHelper redisInvalidationHelper;

        @Mock
        private SettingHelper settingHelper;

        @InjectMocks
        private SAAttendanceServiceImpl attendanceService;

        @BeforeEach
        void setUp() {
                // Mock SettingHelper to return expected values
                when(settingHelper.getSetting(eq(SettingKeys.ATTENDANCE_EARLY_CHECKIN), eq(Integer.class)))
                                .thenReturn(15);
                when(settingHelper.getSetting(eq(SettingKeys.FACE_THRESHOLD_CHECKIN), eq(Double.class)))
                                .thenReturn(0.7);
        }

        @Test
        @DisplayName("Test getAllList should return paginated attendance records")
        void testGetAllList() {
                // Given
                String facilityId = "facility-123";
                String userId = "user-123";
                SAFilterAttendanceRequest request = new SAFilterAttendanceRequest();

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);

                SAAttendanceResponse attendanceResponse = mock(SAAttendanceResponse.class);
                List<SAAttendanceResponse> attendances = Collections.singletonList(attendanceResponse);
                Page<SAAttendanceResponse> page = new PageImpl<>(attendances);

                when(attendanceRepository.getAllByFilter(any(Pageable.class), any(SAFilterAttendanceRequest.class)))
                                .thenReturn(page);

                // When
                ResponseEntity<?> response = attendanceService.getAllList(request);

                // Then
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

                // Verify request fields were properly set
                assertEquals(facilityId, request.getIdFacility());
                assertEquals(userId, request.getIdUserStudent());

                // Verify repository was called
                verify(attendanceRepository).getAllByFilter(any(Pageable.class), eq(request));
        }

        @Test
        @DisplayName("Test checkin should return error when plan date not found")
        void testCheckinPlanDateNotFound() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Không tìm thấy lịch", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when user not registered for factory")
        void testCheckinUserNotInFactory() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                // Create PlanDate with necessary structure
                PlanDate planDate = new PlanDate();
                planDate.setId(planDateId);

                PlanFactory planFactory = new PlanFactory();
                Factory factory = new Factory();
                factory.setId(factoryId);

                Project project = new Project();
                SubjectFacility subjectFacility = new SubjectFacility();
                Facility facility = new Facility();
                facility.setId(facilityId);

                subjectFacility.setFacility(facility);
                project.setSubjectFacility(subjectFacility);
                factory.setProject(project);
                planFactory.setFactory(factory);
                planDate.setPlanFactory(planFactory);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Ca không tồn tại", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when IP validation fails")
        void testCheckinIpValidationFails() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";
                String clientIp = "192.168.1.1";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                // Create objects
                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredIp(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = new UserStudentFactory();
                UserStudent userStudent = new UserStudent();
                userStudent.setId(userId);
                userStudentFactory.setUserStudent(userStudent);
                userStudentFactory.setFactory(planDate.getPlanFactory().getFactory());

                Set<String> allowedIps = new HashSet<>();
                allowedIps.add("10.0.0.1");

                // Configure mocks
                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(facilityIPRepository.getAllIP(facilityId)).thenReturn(allowedIps);
                when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

                try (MockedStatic<AppUtils> appUtilsMock = mockStatic(AppUtils.class)) {
                        appUtilsMock.when(() -> AppUtils.getClientIP(any())).thenReturn(clientIp);

                        // When
                        ResponseEntity<?> response = attendanceService.checkin(request);

                        // Then
                        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                        ApiResponse apiResponse = (ApiResponse) response.getBody();
                        assertNotNull(apiResponse);
                        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                        assertEquals("Vui lòng kết nối bằng mạng trường để tiếp tục checkin/checkout",
                                        apiResponse.getMessage());
                }
        }

        @Test
        @DisplayName("Test checkin should return error when plan date facility mismatch")
        void testCheckinPlanDateFacilityMismatch() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String differentFacilityId = "facility-456";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, differentFacilityId, "factory-123");

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Không tìm thấy lịch", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when user student factory not found")
        void testCheckinUserStudentFactoryNotFound() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Ca không tồn tại", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when client IP is null")
        void testCheckinClientIPNull() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredIp(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(AppUtils.getClientIP(httpServletRequest)).thenReturn(null);

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("IP đăng nhập không hợp lệ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when IP not allowed")
        void testCheckinIPNotAllowed() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";
                String clientIP = "192.168.1.100";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredIp(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                Set<String> allowedIPs = new HashSet<>();
                allowedIPs.add("192.168.1.200");
                allowedIPs.add("192.168.1.201");

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(AppUtils.getClientIP(httpServletRequest)).thenReturn(clientIP);
                when(facilityIPRepository.getAllIP(facilityId)).thenReturn(allowedIPs);

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Vui lòng kết nối bằng mạng trường để tiếp tục checkin/checkout",
                                apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when location is required but coordinates are null")
        void testCheckinLocationRequiredButCoordinatesNull() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);
                request.setLatitude(null);
                request.setLongitude(null);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredLocation(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Không thể lấy thông tin vị trí", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when location is outside allowed area")
        void testCheckinLocationOutsideAllowedArea() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);
                request.setLatitude(10.123);
                request.setLongitude(106.456);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredLocation(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(facilityLocationRepository.getAllList(facilityId)).thenReturn(Collections.emptyList());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Đã quá giờ checkin đầu giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when user has no face embedding")
        void testCheckinNoFaceEmbedding() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);
                userStudentFactory.getUserStudent().setFaceEmbedding(null);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Tài khoản chưa đăng ký thông tin khuôn mặt", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when attendance already present")
        void testCheckinAttendanceAlreadyPresent() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                Attendance existingAttendance = new Attendance();
                existingAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.of(existingAttendance));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Ca đã được điểm danh", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when attendance is absent")
        void testCheckinAttendanceAbsent() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                Attendance existingAttendance = new Attendance();
                existingAttendance.setAttendanceStatus(AttendanceStatus.ABSENT);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.of(existingAttendance));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Bạn đã bị huỷ điểm danh.", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too early for checkin")
        void testCheckinTooEarly() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                planDate.setStartDate(System.currentTimeMillis() + 3600000); // 1 hour from now

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Chưa đến giờ checkin đầu giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too late for checkin")
        void testCheckinTooLate() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                planDate.setStartDate(System.currentTimeMillis() - 7200000); // 2 hours ago
                planDate.setLateArrival(30); // 30 minutes late allowance

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());
                when(attendanceRepository.getAttendanceRecovery(planDateId, userId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Đã quá giờ checkin đầu giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too early for checkout")
        void testCheckinTooEarlyForCheckout() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.DISABLE);
                planDate.setRequiredCheckout(StatusType.ENABLE);
                planDate.setEndDate(System.currentTimeMillis() + 3600000); // 1 hour from now

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Chưa đến giờ checkout cuối giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too late for checkout")
        void testCheckinTooLateForCheckout() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.DISABLE);
                planDate.setRequiredCheckout(StatusType.ENABLE);
                planDate.setEndDate(System.currentTimeMillis() - 7200000); // 2 hours ago
                planDate.setLateArrival(30); // 30 minutes late allowance

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());
                when(attendanceRepository.getAttendanceRecovery(planDateId, userId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Đã quá giờ checkout cuối giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when trying to checkout without checkin")
        void testCheckinCheckoutWithoutCheckin() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.ENABLE);
                planDate.setEndDate(System.currentTimeMillis() + 3600000); // 1 hour from now

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                Attendance existingAttendance = new Attendance();
                existingAttendance.setAttendanceStatus(AttendanceStatus.NOTCHECKIN);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.of(existingAttendance));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Đã quá giờ checkin đầu giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too early for checkout with existing attendance")
        void testCheckinTooEarlyForCheckoutWithExistingAttendance() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.ENABLE);
                planDate.setEndDate(System.currentTimeMillis() + 3600000); // 1 hour from now

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                Attendance existingAttendance = new Attendance();
                existingAttendance.setAttendanceStatus(AttendanceStatus.CHECKIN);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.of(existingAttendance));

                // When
                ResponseEntity<?> response = attendanceService.checkin(request);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Chưa đến giờ checkout cuối giờ", apiResponse.getMessage());
        }

        // Helper method to create a PlanDate with all required nested objects
        private PlanDate createPlanDate(String planDateId, String facilityId, String factoryId) {
                PlanDate planDate = new PlanDate();
                planDate.setId(planDateId);

                PlanFactory planFactory = new PlanFactory();
                Factory factory = new Factory();
                factory.setId(factoryId);
                Project project = new Project();
                SubjectFacility subjectFacility = new SubjectFacility();
                Facility facility = new Facility();
                facility.setId(facilityId);

                subjectFacility.setFacility(facility);
                project.setSubjectFacility(subjectFacility);
                factory.setProject(project);
                planFactory.setFactory(factory);
                planDate.setPlanFactory(planFactory);

                // Set default values
                planDate.setType(ShiftType.OFFLINE);
                planDate.setRequiredIp(StatusType.DISABLE);
                planDate.setRequiredLocation(StatusType.DISABLE);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.ENABLE);
                planDate.setStartDate(System.currentTimeMillis() - 3600000); // 1 hour ago
                planDate.setEndDate(System.currentTimeMillis() + 3600000); // 1 hour from now
                planDate.setLateArrival(15); // 15 minutes

                return planDate;
        }

        private UserStudentFactory createUserStudentFactory(String userId, String factoryId) {
                UserStudent userStudent = new UserStudent();
                userStudent.setId(userId);
                userStudent.setFaceEmbedding("face-embedding-data");

                Factory factory = new Factory();
                factory.setId(factoryId);

                UserStudentFactory userStudentFactory = new UserStudentFactory();
                userStudentFactory.setUserStudent(userStudent);
                userStudentFactory.setFactory(factory);

                return userStudentFactory;
        }
}
