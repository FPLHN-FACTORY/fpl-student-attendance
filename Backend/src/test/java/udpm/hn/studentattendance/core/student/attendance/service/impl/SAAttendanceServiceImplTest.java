package udpm.hn.studentattendance.core.student.attendance.service.impl;

import ai.djl.translate.TranslateException;
import jakarta.servlet.http.HttpServletRequest;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        private udpm.hn.studentattendance.infrastructure.common.services.OnnxService onnxService;

        private org.springframework.web.multipart.MultipartFile mockImage;

        private org.springframework.web.multipart.MultipartFile mockImage2;

        @InjectMocks
        private SAAttendanceServiceImpl attendanceService;

        @BeforeEach
        void setUp() {
                // Create a custom OnnxService mock that doesn't throw exceptions
                onnxService = new udpm.hn.studentattendance.infrastructure.common.services.OnnxService() {
                        @Override
                        public float[] getEmbedding(byte[] imgBytes) {
                                return new float[] { 0.1f, 0.2f, 0.3f };
                        }

                        @Override
                        public boolean isFake(byte[] faceBox, byte[] canvas) {
                                return false;
                        }
                };

                // Manually inject the onnxService into the attendanceService
                ReflectionTestUtils.setField(attendanceService, "onnxService", onnxService);

                // Mock SettingHelper to return expected values
                lenient().when(settingHelper.getSetting(eq(SettingKeys.ATTENDANCE_EARLY_CHECKIN), eq(Integer.class)))
                                .thenReturn(15);
                lenient().when(settingHelper.getSetting(eq(SettingKeys.FACE_THRESHOLD_CHECKIN), eq(Double.class)))
                                .thenReturn(0.7);

                // Create custom MultipartFile implementations that don't throw exceptions
                mockImage = createMockMultipartFile("image", "image.jpg", false, 1024L);
                mockImage2 = createMockMultipartFile("image2", "image2.jpg", false, 1024L);
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

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

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

                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Ca không tồn tại", apiResponse.getMessage());
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
                planDate.setStartDate(System.currentTimeMillis() + 7200000); // 2 hours from now

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserId()).thenReturn(userId);
                when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Chưa đến giờ checkin đầu giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when too late")
        void testCheckinTooLate() throws TranslateException, InterruptedException {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                planDate.setStartDate(System.currentTimeMillis() - 7200000); // 2 hours ago (too late)

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());
                lenient().when(attendanceRepository.getAttendanceRecovery(planDateId, userId))
                                .thenReturn(Optional.empty());

                // onnxService is already mocked in @BeforeEach

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

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
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

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
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Đã quá giờ checkout cuối giờ", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when IP validation fails")
        void testCheckinIPValidationFailed() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";
                String clientIP = "192.168.2.100"; // IP not in allowed list

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredIp(StatusType.ENABLE); // Enable IP validation
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());
                lenient().when(httpServletRequest.getHeader("CF-Connecting-IP")).thenReturn(clientIP);
                lenient().when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(clientIP);
                lenient().when(httpServletRequest.getRemoteAddr()).thenReturn(clientIP);
                lenient().when(facilityIPRepository.getAllIP(facilityId))
                                .thenReturn(Set.of("192.168.1.1", "192.168.1.2")); // Different IPs

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Vui lòng kết nối bằng mạng trường để tiếp tục checkin/checkout",
                                apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when location validation fails")
        void testCheckinLocationValidationFailed() throws TranslateException, InterruptedException {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);
                request.setLatitude(10.123);
                request.setLongitude(106.456);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                planDate.setRequiredLocation(StatusType.ENABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);
                // Set a valid face embedding so validation can proceed to location validation
                userStudentFactory.getUserStudent().setFaceEmbedding("face-embedding-data");

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());
                lenient().when(facilityLocationRepository.getAllList(facilityId))
                                .thenReturn(Collections.emptyList()); // No allowed locations

                // onnxService is already mocked in @BeforeEach

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Thông tin khuôn mặt không hợp lệ. Vui lòng thử lại", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when face validation fails")
        void testCheckinFaceValidationFailed() throws TranslateException, InterruptedException {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);
                userStudentFactory.getUserStudent().setFaceEmbedding(null); // No face embedding

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Tài khoản chưa đăng ký thông tin khuôn mặt", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when image is empty")
        void testCheckinImageEmpty() throws TranslateException, InterruptedException {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);
                // Set a valid face embedding so validation can proceed to image validation
                userStudentFactory.getUserStudent().setFaceEmbedding("0.1,0.2,0.3,0.4,0.5");

                // Create a new image that is empty for this test
                mockImage = createMockMultipartFile("image", "image.jpg", true, 0L);

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Thông tin khuôn mặt không hợp lệ. Vui lòng thử lại", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when image is too large")
        void testCheckinImageTooLarge() throws TranslateException, InterruptedException {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);

                UserStudentFactory userStudentFactory = createUserStudentFactory(userId, factoryId);
                // Set a valid face embedding so validation can proceed to image validation
                userStudentFactory.getUserStudent().setFaceEmbedding("0.1,0.2,0.3,0.4,0.5");

                // Create a new image that is too large for this test
                mockImage = createMockMultipartFile("image", "image.jpg", false, 10 * 1024 * 1024L);

                lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                lenient().when(sessionHelper.getUserId()).thenReturn(userId);
                lenient().when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
                lenient().when(userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(userId, factoryId))
                                .thenReturn(Optional.of(userStudentFactory));
                lenient().when(attendanceRepository.findByUserStudent_IdAndPlanDate_Id(userId, planDateId))
                                .thenReturn(Optional.empty());

                // When
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Thông tin khuôn mặt không hợp lệ. Vui lòng thử lại", apiResponse.getMessage());
        }

        @Test
        @DisplayName("Test checkin should return error when existing attendance found")
        void testCheckinExistingAttendanceFound() {
                // Given
                String planDateId = "plan-date-123";
                String facilityId = "facility-123";
                String userId = "user-123";
                String factoryId = "factory-123";

                SACheckinAttendanceRequest request = new SACheckinAttendanceRequest();
                request.setIdPlanDate(planDateId);

                PlanDate planDate = createPlanDateForValidationTest(planDateId, facilityId, factoryId);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                // Set end time in the past so checkout time validation passes
                planDate.setEndDate(System.currentTimeMillis() - 1800000); // 30 minutes ago

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
                ResponseEntity<?> response = attendanceService.checkin(request, mockImage, mockImage2);

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Ca đã được điểm danh", apiResponse.getMessage());
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

                // Set default values - ensure time validation passes
                planDate.setType(ShiftType.OFFLINE);
                planDate.setRequiredIp(StatusType.DISABLE);
                planDate.setRequiredLocation(StatusType.DISABLE);
                planDate.setRequiredCheckin(StatusType.ENABLE);
                planDate.setRequiredCheckout(StatusType.DISABLE);
                planDate.setStartDate(System.currentTimeMillis() - 900000); // 15 minutes ago (within early checkin
                                                                            // window)
                planDate.setEndDate(System.currentTimeMillis() + 3600000); // 1 hour from now
                planDate.setLateArrival(15); // 15 minutes

                return planDate;
        }

        // Helper method to create a PlanDate that passes time validation for specific
        // tests
        private PlanDate createPlanDateForValidationTest(String planDateId, String facilityId, String factoryId) {
                PlanDate planDate = createPlanDate(planDateId, facilityId, factoryId);
                // Ensure time validation passes by setting start time to be within the allowed
                // window
                planDate.setStartDate(System.currentTimeMillis() - 900000); // 15 minutes ago
                planDate.setEndDate(System.currentTimeMillis() + 7200000); // 2 hours from now
                planDate.setLateArrival(30); // 30 minutes late allowance
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

        // Helper method to create configurable MultipartFile implementations
        private org.springframework.web.multipart.MultipartFile createMockMultipartFile(String name,
                        String originalFilename, boolean isEmpty, long size) {
                return new org.springframework.web.multipart.MultipartFile() {
                        @Override
                        public String getName() {
                                return name;
                        }

                        @Override
                        public String getOriginalFilename() {
                                return originalFilename;
                        }

                        @Override
                        public String getContentType() {
                                return "image/jpeg";
                        }

                        @Override
                        public boolean isEmpty() {
                                return isEmpty;
                        }

                        @Override
                        public long getSize() {
                                return size;
                        }

                        @Override
                        public byte[] getBytes() throws IOException {
                                return new byte[1024];
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                                return new ByteArrayInputStream(new byte[1024]);
                        }

                        @Override
                        public void transferTo(File dest) throws IOException, IllegalStateException {
                        }
                };
        }
}
