package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationSemesterRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.utils.FaceRecognitionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.HashMap;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private HttpSession httpSession;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticationFacilityRepository authenticationFacilityRepository;

    @Mock
    private AuthenticationSemesterRepository authenticationSemesterRepository;

    @Mock
    private AuthenticationUserAdminRepository authenticationUserAdminRepository;

    @Mock
    private AuthenticationUserStaffRepository authenticationUserStaffRepository;

    @Mock
    private AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Mock
    private SettingHelper settingHelper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "settingHelper", settingHelper);
        // Removed unnecessary stubbing for settingHelper.getSetting
        // ... rest of your setup code ...
    }

    @Test
    @DisplayName("Test authorSwitch should set session attributes and redirect")
    void testAuthorSwitch() {
        // Given
        String role = "STUDENT";
        String redirectUri = "/student/dashboard";
        String facilityId = "facility-123";

        // When
        RedirectView result = authenticationService.authorSwitch(role, redirectUri, facilityId);

        // Then
        verify(httpSession).setAttribute(SessionConstant.LOGIN_ROLE, role);
        verify(httpSession).setAttribute(SessionConstant.LOGIN_REDIRECT, redirectUri);
        verify(httpSession).setAttribute(SessionConstant.LOGIN_FACILITY, facilityId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test getAllFacility should return list of active facilities")
    void testGetAllFacility() {
        // Given
        List<Facility> facilities = new ArrayList<>();
        Facility facility = new Facility();
        facility.setId("facility-123");
        facility.setName("FPT HCM");
        facility.setCode("HCM");
        facilities.add(facility);

        when(authenticationFacilityRepository.findAllByStatusOrderByPositionAsc(EntityStatus.ACTIVE))
                .thenReturn(facilities);

        // When
        ResponseEntity<?> response = authenticationService.getAllFacility();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải dữ liệu danh sách cơ sở thành công", apiResponse.getMessage());
        assertEquals(facilities, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getAllSemester should return list of active semesters")
    void testGetAllSemester() {
        // Given
        List<Semester> semesters = new ArrayList<>();
        Semester semester = new Semester();
        semester.setId("semester-123");
        semester.setCode("Spring 2023");
        semesters.add(semester);

        when(authenticationSemesterRepository.findAllByStatusOrderByFromDateDesc(EntityStatus.ACTIVE))
                .thenReturn(semesters);

        // When
        ResponseEntity<?> response = authenticationService.getAllSemester();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải dữ liệu danh sách học kỳ thành công", apiResponse.getMessage());
        assertEquals(semesters, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getInfoUser for ADMIN role should return admin user info")
    void testGetInfoUserWithAdminRole() {
        // Given
        String role = "ADMIN";
        String facilityId = "facility-123";
        String email = "admin@example.com";
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("admin-123");
        userAdmin.setEmail(email);
        userAdmin.setName("Admin User");

        AuthUser authUser = new AuthUser();
        authUser.setId("admin-123");
        authUser.setName("Admin User");
        authUser.setEmail(email);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(email);
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.ADMIN));
        when(authenticationUserAdminRepository.findByEmail(email)).thenReturn(Optional.of(userAdmin));
        when(sessionHelper.buildAuthUser(userAdmin, Set.of(RoleConstant.ADMIN), facilityId)).thenReturn(authUser);

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy thông tin người dùng thành công", apiResponse.getMessage());
        assertEquals(authUser, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when role is null")
    void testGetInfoUserWithNullRole() {
        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when admin not found")
    void testGetInfoUserWithAdminNotFound() {
        // Given
        String role = "ADMIN";
        String email = "admin@example.com";

        when(sessionHelper.getUserEmail()).thenReturn(email);
        when(authenticationUserAdminRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when staff not found")
    void testGetInfoUserWithStaffNotFound() {
        // Given
        String role = "STAFF";
        String facilityId = "facility-123";
        String email = "staff@fpt.edu.vn";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmailFpt()).thenReturn(email);
        when(authenticationUserStaffRepository.findLogin(email, facilityId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when teacher not found")
    void testGetInfoUserWithTeacherNotFound() {
        // Given
        String role = "TEACHER";
        String facilityId = "facility-123";
        String email = "teacher@fpt.edu.vn";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmailFpt()).thenReturn(email);
        when(authenticationUserStaffRepository.findLogin(email, facilityId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when student not found with facility")
    void testGetInfoUserWithStudentNotFoundWithFacility() {
        // Given
        String role = "STUDENT";
        String facilityId = "facility-123";
        String email = "student@fpt.edu.vn";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(email);
        when(authenticationUserStudentRepository.findByEmailAndFacility_Id(email, facilityId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when student not found without facility")
    void testGetInfoUserWithStudentNotFoundWithoutFacility() {
        // Given
        String role = "STUDENT";
        String facilityId = "null";
        String email = "student@fpt.edu.vn";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(email);
        when(authenticationUserStudentRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should successfully register a student")
    void testStudentRegisterSuccessfully() {
        try (MockedStatic<FaceRecognitionUtils> faceRecognitionUtilsMocked = Mockito
                .mockStatic(FaceRecognitionUtils.class)) {
            // Given
            String studentId = "student-123";
            String facilityId = "facility-123";
            String email = "student@example.com";
            String faceEmbeddingStr = "[0.1, 0.2, 0.3]";

            double[] faceEmbedding = new double[] { 0.1, 0.2, 0.3 };
            List<double[]> faceEmbeddings = List.of(faceEmbedding);

            UserStudent student = new UserStudent();
            student.setId(studentId);
            student.setEmail(email);

            Facility facility = new Facility();
            facility.setId(facilityId);
            facility.setName("FPT HCM");

            AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
            request.setIdFacility(facilityId);
            request.setCode("SE12345");
            request.setName("Student Name");
            request.setFaceEmbedding(faceEmbeddingStr);

            AuthUser authUser = new AuthUser();
            authUser.setId(studentId);
            authUser.setEmail(email);

            // Mock face recognition utils
            faceRecognitionUtilsMocked.when(() -> FaceRecognitionUtils.parseEmbeddings(faceEmbeddingStr))
                    .thenReturn(faceEmbeddings);
            faceRecognitionUtilsMocked
                    .when(() -> FaceRecognitionUtils.isSameFaceAndResult(anyList(), any(double[].class), anyDouble()))
                    .thenReturn(null);
            faceRecognitionUtilsMocked
                    .when(() -> FaceRecognitionUtils.isSameFace(any(double[].class), any(double[].class), anyDouble()))
                    .thenReturn(false);

            // Mock repository methods
            when(sessionHelper.getUserId()).thenReturn(studentId);
            when(authenticationUserStudentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(authenticationFacilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
            when(authenticationUserStudentRepository.isExistsCode(request.getCode(), studentId, facilityId))
                    .thenReturn(false);
            when(authenticationUserStudentRepository.getAllFaceEmbedding(facilityId)).thenReturn(new ArrayList<>());
            when(sessionHelper.buildAuthUser(any(UserStudent.class), anySet(), anyString())).thenReturn(authUser);
            when(jwtUtil.generateToken(anyString(), any(AuthUser.class))).thenReturn("access-token");
            when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(authenticationUserStudentRepository.save(any(UserStudent.class))).thenAnswer(invocation -> {
                UserStudent savedStudent = invocation.getArgument(0);
                savedStudent.setId(studentId);
                return savedStudent;
            });

            // When
            ResponseEntity<?> response = authenticationService.studentRegister(request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApiResponse apiResponse = (ApiResponse) response.getBody();
            assertNotNull(apiResponse);
            assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
            assertEquals("Đăng ký thông tin sinh viên thành công", apiResponse.getMessage());

            AuthenticationToken token = (AuthenticationToken) apiResponse.getData();
            assertEquals("access-token", token.getAccessToken());
            assertEquals("refresh-token", token.getRefreshToken());

            verify(authenticationUserStudentRepository).save(student);
        }
    }

    @Test
    @DisplayName("Test studentRegister should return error when student not found")
    void testStudentRegisterWithStudentNotFound() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding("face-data");

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when student already exists")
    void testStudentRegisterWithStudentAlreadyExists() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding("face-data");

        UserStudent student = new UserStudent();
        student.setId(userId);
        student.setFacility(new Facility()); // Student already has facility

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sinh viên đã tồn tại trên hệ thống", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when facility not found")
    void testStudentRegisterWithFacilityNotFound() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding("face-data");

        UserStudent student = new UserStudent();
        student.setId(userId);
        student.setFacility(null);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById("facility-123")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Cơ sở không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when student code already exists")
    void testStudentRegisterWithCodeAlreadyExists() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding("face-data");

        UserStudent student = new UserStudent();
        student.setId(userId);
        student.setFacility(null);

        Facility facility = new Facility();
        facility.setId("facility-123");

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById("facility-123")).thenReturn(Optional.of(facility));
        when(authenticationUserStudentRepository.isExistsCode("ST001", userId, "facility-123")).thenReturn(true);

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Mã số sinh viên đã tồn tại trên cơ sở này", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when face embedding is null")
    void testStudentRegisterWithNullFaceEmbedding() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding(null);

        UserStudent student = new UserStudent();
        student.setId(userId);
        student.setFacility(null);

        Facility facility = new Facility();
        facility.setId("facility-123");

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById("facility-123")).thenReturn(Optional.of(facility));
        when(authenticationUserStudentRepository.isExistsCode("ST001", userId, "facility-123")).thenReturn(false);

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin khuôn mặt không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when face embedding is empty")
    void testStudentRegisterWithEmptyFaceEmbedding() {
        // Given
        String userId = "student-123";
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-123");
        request.setCode("ST001");
        request.setFaceEmbedding("");

        UserStudent student = new UserStudent();
        student.setId(userId);
        student.setFacility(null);

        Facility facility = new Facility();
        facility.setId("facility-123");

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById("facility-123")).thenReturn(Optional.of(facility));
        when(authenticationUserStudentRepository.isExistsCode("ST001", userId, "facility-123")).thenReturn(false);

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin khuôn mặt không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentInfo should return student information")
    void testStudentInfo() {
        // Given
        String studentId = "student-123";
        String facilityId = "facility-123";

        UserStudent student = new UserStudent();
        student.setId(studentId);
        student.setFaceEmbedding("[0.1, 0.2, 0.3]");

        Facility facility = new Facility();
        facility.setId(facilityId);
        student.setFacility(facility);

        when(sessionHelper.getUserId()).thenReturn(studentId);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(authenticationUserStudentRepository.findById(studentId)).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentInfo();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy thông tinh sinh viên thành công", apiResponse.getMessage());

        UserStudent resultStudent = (UserStudent) apiResponse.getData();
        assertEquals("OK", resultStudent.getFaceEmbedding()); // FaceEmbedding should be replaced with "OK"
    }

    @Test
    @DisplayName("Test studentUpdateFaceID should update student's face ID")
    void testStudentUpdateFaceID() {
        try (MockedStatic<FaceRecognitionUtils> faceRecognitionUtilsMocked = Mockito
                .mockStatic(FaceRecognitionUtils.class)) {
            // Given
            String studentId = "student-123";
            String facilityId = "facility-123";
            String faceEmbeddingStr = "[0.1, 0.2, 0.3]";

            double[] faceEmbedding = new double[] { 0.1, 0.2, 0.3 };
            List<double[]> faceEmbeddings = List.of(faceEmbedding);

            UserStudent student = new UserStudent();
            student.setId(studentId);
            // Ensure face embedding is null to pass the hasText check
            student.setFaceEmbedding(null);

            AuthenticationStudentUpdateFaceIDRequest request = new AuthenticationStudentUpdateFaceIDRequest();
            request.setFaceEmbedding(faceEmbeddingStr);

            // Mock face recognition utils
            faceRecognitionUtilsMocked.when(() -> FaceRecognitionUtils.parseEmbeddings(faceEmbeddingStr))
                    .thenReturn(faceEmbeddings);
            faceRecognitionUtilsMocked.when(() -> FaceRecognitionUtils.parseEmbedding(anyString()))
                    .thenReturn(faceEmbedding);
            faceRecognitionUtilsMocked
                    .when(() -> FaceRecognitionUtils.isSameFaceAndResult(anyList(), any(double[].class), anyDouble()))
                    .thenReturn(null);
            faceRecognitionUtilsMocked
                    .when(() -> FaceRecognitionUtils.isSameFace(any(double[].class), any(double[].class), anyDouble()))
                    .thenReturn(false);

            when(sessionHelper.getUserId()).thenReturn(studentId);
            when(sessionHelper.getFacilityId()).thenReturn(facilityId);
            when(authenticationUserStudentRepository.findById(studentId)).thenReturn(Optional.of(student));

            // Mock getAllFaceEmbedding to return a list with at least one element
            List<String> faceEmbeddingsStr = List.of("[0.4, 0.5, 0.6]");
            when(authenticationUserStudentRepository.getAllFaceEmbedding(facilityId)).thenReturn(faceEmbeddingsStr);

            when(authenticationUserStudentRepository.save(student)).thenReturn(student);

            // Add this stub to prevent ClassCastException
            when(settingHelper.getSetting(any(), eq(Double.class))).thenReturn(0.5);

            // When
            ResponseEntity<?> response = authenticationService.studentUpdateFaceID(request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApiResponse apiResponse = (ApiResponse) response.getBody();
            assertNotNull(apiResponse);
            assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
            assertEquals("Cập nhật khuôn mặt thành công", apiResponse.getMessage());

            verify(notificationService).add(any());
            verify(authenticationUserStudentRepository).save(student);
        }
    }

    @Test
    @DisplayName("Test studentUpdateFaceID should return error when student not found")
    void testStudentUpdateFaceIDWithStudentNotFound() {
        // Given
        String userId = "student-123";
        AuthenticationStudentUpdateFaceIDRequest request = new AuthenticationStudentUpdateFaceIDRequest();
        request.setFaceEmbedding("face-data");

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentUpdateFaceID(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentUpdateFaceID should return error when face embedding is null")
    void testStudentUpdateFaceIDWithNullFaceEmbedding() {
        // Given
        String userId = "student-123";
        AuthenticationStudentUpdateFaceIDRequest request = new AuthenticationStudentUpdateFaceIDRequest();
        request.setFaceEmbedding(null);

        UserStudent student = new UserStudent();
        student.setId(userId);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentUpdateFaceID(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin khuôn mặt không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentUpdateFaceID should return error when face embedding is empty")
    void testStudentUpdateFaceIDWithEmptyFaceEmbedding() {
        // Given
        String userId = "student-123";
        AuthenticationStudentUpdateFaceIDRequest request = new AuthenticationStudentUpdateFaceIDRequest();
        request.setFaceEmbedding("");

        UserStudent student = new UserStudent();
        student.setId(userId);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(authenticationUserStudentRepository.findById(userId)).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentUpdateFaceID(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin khuôn mặt không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test refreshToken should return new token pair")
    void testRefreshToken() {
        // Given
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.generateToken(refreshToken)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(newAccessToken)).thenReturn(newRefreshToken);

        // When
        ResponseEntity<?> response = authenticationService.refreshToken(refreshToken);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Gia hạn token thành công", apiResponse.getMessage());

        AuthenticationToken token = (AuthenticationToken) apiResponse.getData();
        assertEquals(newAccessToken, token.getAccessToken());
        assertEquals(newRefreshToken, token.getRefreshToken());
    }

    @Test
    @DisplayName("Test refreshToken should return error for invalid token")
    void testRefreshTokenWithInvalidToken() {
        // Given
        String invalidToken = "invalid-token";

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When
        ResponseEntity<?> response = authenticationService.refreshToken(invalidToken);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Refresh Token không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test refreshToken should return error when refresh token is null")
    void testRefreshTokenWithNullToken() {
        // When
        ResponseEntity<?> response = authenticationService.refreshToken(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Refresh Token không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test refreshToken should return error when refresh token is empty")
    void testRefreshTokenWithEmptyToken() {
        // When
        ResponseEntity<?> response = authenticationService.refreshToken("");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Refresh Token không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when required fields are null")
    void testSaveSettingsWithNullRequiredFields() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, null);
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng nhập đầy đủ các trường bắt buộc", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when shiftMinDiff is too low")
    void testSaveSettingsWithShiftMinDiffTooLow() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "0"); // Too low
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian diễn ra ca học tối thiểu không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when shiftMinDiff is too high")
    void testSaveSettingsWithShiftMinDiffTooHigh() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "500"); // Too high
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian diễn ra ca học tối thiểu không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when shiftMaxLateArrival is too low")
    void testSaveSettingsWithShiftMaxLateArrivalTooLow() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "3"); // Too low
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian điểm danh muộn nhất không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when shiftMaxLateArrival is too high")
    void testSaveSettingsWithShiftMaxLateArrivalTooHigh() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "100"); // Too high
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian điểm danh muộn nhất không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when attendanceEarlyCheckin is too high")
    void testSaveSettingsWithAttendanceEarlyCheckinTooHigh() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "35"); // Too high
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian cho phép checkin sớm không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when expirationMinuteLogin is too low")
    void testSaveSettingsWithExpirationMinuteLoginTooLow() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "50"); // Too low
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời hạn phiên đăng nhập không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when faceThresholdCheckin is zero")
    void testSaveSettingsWithFaceThresholdCheckinZero() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.0"); // Zero
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Độ khắt khe checkin/checkout không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when faceThresholdCheckin is too high")
    void testSaveSettingsWithFaceThresholdCheckinTooHigh() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "1.5"); // Too high
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.8");

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Độ khắt khe checkin/checkout không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when faceThresholdRegister is zero")
    void testSaveSettingsWithFaceThresholdRegisterZero() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "0.0"); // Zero

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Độ khắt khe đăng ký mặt không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test saveSettings should return error when faceThresholdRegister is too high")
    void testSaveSettingsWithFaceThresholdRegisterTooHigh() {
        // Given
        Map<SettingKeys, String> settings = new HashMap<>();
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "false");
        settings.put(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, "true");
        settings.put(SettingKeys.SHIFT_MIN_DIFF, "60");
        settings.put(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, "30");
        settings.put(SettingKeys.ATTENDANCE_EARLY_CHECKIN, "15");
        settings.put(SettingKeys.EXPIRATION_MINUTE_LOGIN, "120");
        settings.put(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.7");
        settings.put(SettingKeys.FACE_THRESHOLD_REGISTER, "1.2"); // Too high

        // When
        ResponseEntity<?> response = authenticationService.saveSettings(settings);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Độ khắt khe đăng ký mặt không hợp lệ", apiResponse.getMessage());
    }
}