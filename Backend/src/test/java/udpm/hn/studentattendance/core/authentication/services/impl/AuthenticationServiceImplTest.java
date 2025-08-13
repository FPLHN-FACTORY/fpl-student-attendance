package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
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
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private HttpSession httpSession;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private SettingHelper settingHelper;

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
    private org.springframework.web.multipart.MultipartFile mockImage;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        // Setup common mocks if needed
    }

    @Test
    @DisplayName("Test authorSwitch should set session attributes and return redirect view")
    void testAuthorSwitch() {
        // Given
        String role = "ADMIN";
        String redirectUri = "http://localhost:3000";
        String facilityId = "facility-1";

        // When
        RedirectView result = authenticationService.authorSwitch(role, redirectUri, facilityId);

        // Then
        verify(httpSession).setAttribute(SessionConstant.LOGIN_ROLE, role);
        verify(httpSession).setAttribute(SessionConstant.LOGIN_REDIRECT, redirectUri);
        verify(httpSession).setAttribute(SessionConstant.LOGIN_FACILITY, facilityId);
        assertEquals(RouteAuthenticationConstant.REDIRECT_GOOGLE_AUTHORIZATION, result.getUrl());
    }

    @Test
    @DisplayName("Test getAllFacility should return active facilities")
    void testGetAllFacility() {
        // Given
        List<Facility> facilities = Arrays.asList(
                createMockFacility("facility-1", "FPT HCM"),
                createMockFacility("facility-2", "FPT HN"));
        when(authenticationFacilityRepository.findAllByStatusOrderByPositionAsc(EntityStatus.ACTIVE))
                .thenReturn(facilities);

        // When
        ResponseEntity<?> response = authenticationService.getAllFacility();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tải dữ liệu danh sách cơ sở thành công", apiResponse.getMessage());
        assertEquals(facilities, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getAllSemester should return active semesters")
    void testGetAllSemester() {
        // Given
        List<Object> semesters = Arrays.asList("Semester 1", "Semester 2");
        when(authenticationSemesterRepository.findAllByStatusOrderByFromDateDesc(EntityStatus.ACTIVE))
                .thenReturn((List) semesters);

        // When
        ResponseEntity<?> response = authenticationService.getAllSemester();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tải dữ liệu danh sách học kỳ thành công", apiResponse.getMessage());
        assertEquals(semesters, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getSettings should return all settings")
    void testGetSettings() {
        // Given
        Map<String, Object> settings = new HashMap<>();
        settings.put("setting1", "value1");
        settings.put("setting2", "value2");
        when(settingHelper.getAllSettings()).thenReturn((Map) settings);

        // When
        ResponseEntity<?> response = authenticationService.getSettings();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy dữ liệu cài đặt thành công", apiResponse.getMessage());
        assertEquals(settings, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when role is null")
    void testGetInfoUserWithNullRole() {
        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Token đăng nhập không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return admin info when role is ADMIN")
    void testGetInfoUserForAdmin() {
        // Given
        String role = "ADMIN";
        String userEmail = "admin@fpt.edu.vn";
        String facilityId = "facility-1";

        UserAdmin userAdmin = createMockUserAdmin();
        AuthUser expectedAuthUser = createMockAuthUser();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(userEmail);
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.ADMIN));
        when(authenticationUserAdminRepository.findByEmail(userEmail)).thenReturn(Optional.of(userAdmin));
        when(sessionHelper.buildAuthUser(userAdmin, Set.of(RoleConstant.ADMIN), facilityId))
                .thenReturn(expectedAuthUser);

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin người dùng thành công", apiResponse.getMessage());
        assertEquals(expectedAuthUser, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getInfoUser should return error when admin not found")
    void testGetInfoUserForAdminNotFound() {
        // Given
        String role = "ADMIN";
        String userEmail = "admin@fpt.edu.vn";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(userEmail);
        when(authenticationUserAdminRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Token đăng nhập không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getInfoUser should return staff info when role is TEACHER")
    void testGetInfoUserForTeacher() {
        // Given
        String role = "TEACHER";
        String userEmailFpt = "teacher@fpt.edu.vn";
        String facilityId = "facility-1";

        UserStaff userStaff = createMockUserStaff();
        AuthUser expectedAuthUser = createMockAuthUser();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmailFpt()).thenReturn(userEmailFpt);
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.TEACHER));
        when(authenticationUserStaffRepository.findLogin(userEmailFpt, facilityId)).thenReturn(Optional.of(userStaff));
        when(sessionHelper.buildAuthUser(userStaff, Set.of(RoleConstant.TEACHER), facilityId))
                .thenReturn(expectedAuthUser);

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin người dùng thành công", apiResponse.getMessage());
        assertEquals(expectedAuthUser, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getInfoUser should return student info when role is STUDENT")
    void testGetInfoUserForStudent() {
        // Given
        String role = "STUDENT";
        String userEmail = "student@fpt.edu.vn";
        String facilityId = "facility-1";

        UserStudent userStudent = createMockUserStudent();
        AuthUser expectedAuthUser = createMockAuthUser();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserEmail()).thenReturn(userEmail);
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.STUDENT));
        when(authenticationUserStudentRepository.findByEmailAndFacility_Id(userEmail, facilityId))
                .thenReturn(Optional.of(userStudent));
        when(sessionHelper.buildAuthUser(userStudent, Set.of(RoleConstant.STUDENT), facilityId))
                .thenReturn(expectedAuthUser);

        // When
        ResponseEntity<?> response = authenticationService.getInfoUser(role);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin người dùng thành công", apiResponse.getMessage());
        assertEquals(expectedAuthUser, apiResponse.getData());
    }

    @Test
    @DisplayName("Test studentRegister should return error when student not found")
    void testStudentRegisterStudentNotFound() {
        // Given
        AuthenticationStudentRegisterRequest request = createMockStudentRegisterRequest();
        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request, mockImage);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when student already has facility")
    void testStudentRegisterStudentAlreadyHasFacility() {
        // Given
        AuthenticationStudentRegisterRequest request = createMockStudentRegisterRequest();
        UserStudent student = createMockUserStudent();
        student.setFacility(createMockFacility("facility-1", "FPT HCM"));

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request, mockImage);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Sinh viên đã tồn tại trên hệ thống", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when facility not found")
    void testStudentRegisterFacilityNotFound() {
        // Given
        AuthenticationStudentRegisterRequest request = createMockStudentRegisterRequest();
        UserStudent student = createMockUserStudent();

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById(request.getIdFacility())).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request, mockImage);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cơ sở không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when student code already exists")
    void testStudentRegisterCodeExists() {
        // Given
        AuthenticationStudentRegisterRequest request = createMockStudentRegisterRequest();
        UserStudent student = createMockUserStudent();
        Facility facility = createMockFacility("facility-1", "FPT HCM");

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById(request.getIdFacility())).thenReturn(Optional.of(facility));
        when(authenticationUserStudentRepository.isExistsCode(request.getCode(), student.getId(), facility.getId()))
                .thenReturn(true);

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request, mockImage);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Mã số sinh viên đã tồn tại trên cơ sở này", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentRegister should return error when image is null")
    void testStudentRegisterNullImage() {
        // Given
        AuthenticationStudentRegisterRequest request = createMockStudentRegisterRequest();
        UserStudent student = createMockUserStudent();
        Facility facility = createMockFacility("facility-1", "FPT HCM");

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(authenticationFacilityRepository.findById(request.getIdFacility())).thenReturn(Optional.of(facility));
        when(authenticationUserStudentRepository.isExistsCode(request.getCode(), student.getId(), facility.getId()))
                .thenReturn(false);

        // When
        ResponseEntity<?> response = authenticationService.studentRegister(request, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thông tin khuôn mặt không hợp lệ. Vui lòng thử lại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test refreshToken should return error when token is invalid")
    void testRefreshTokenInvalidToken() {
        // Given
        String refreshToken = "invalid-token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        // When
        ResponseEntity<?> response = authenticationService.refreshToken(refreshToken);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Refresh Token không hợp lệ hoặc đã hết hạn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test refreshToken should return new tokens when token is valid")
    void testRefreshTokenValidToken() {
        // Given
        String refreshToken = "valid-token";
        String newToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.generateToken(refreshToken)).thenReturn(newToken);
        when(jwtUtil.generateRefreshToken(newToken)).thenReturn(newRefreshToken);

        // When
        ResponseEntity<?> response = authenticationService.refreshToken(refreshToken);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Gia hạn token thành công", apiResponse.getMessage());

        AuthenticationToken result = (AuthenticationToken) apiResponse.getData();
        assertEquals(newToken, result.getAccessToken());
        assertEquals(newRefreshToken, result.getRefreshToken());
    }

    @Test
    @DisplayName("Test studentInfo should return error when student not found")
    void testStudentInfoStudentNotFound() {
        // Given
        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authenticationService.studentInfo();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Vui lòng đăng ký thông tin sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentInfo should return error when student has no facility")
    void testStudentInfoStudentNoFacility() {
        // Given
        UserStudent student = createMockUserStudent();
        student.setFacility(null);

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentInfo();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Vui lòng đăng ký thông tin sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentInfo should return error when facility mismatch")
    void testStudentInfoFacilityMismatch() {
        // Given
        UserStudent student = createMockUserStudent();
        student.setFacility(createMockFacility("facility-1", "FPT HCM"));

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(sessionHelper.getFacilityId()).thenReturn("facility-2");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentInfo();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Vui lòng đăng ký thông tin sinh viên", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test studentInfo should return student info with face embedding")
    void testStudentInfoSuccess() {
        // Given
        UserStudent student = createMockUserStudent();
        student.setFacility(createMockFacility("facility-1", "FPT HCM"));
        student.setFaceEmbedding("face-embedding-data");

        when(sessionHelper.getUserId()).thenReturn("student-1");
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(authenticationUserStudentRepository.findById("student-1")).thenReturn(Optional.of(student));

        // When
        ResponseEntity<?> response = authenticationService.studentInfo();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tinh sinh viên thành công", apiResponse.getMessage());

        UserStudent result = (UserStudent) apiResponse.getData();
        assertNotNull(result.getFaceEmbedding());
    }

    // Helper methods
    private Facility createMockFacility(String id, String name) {
        Facility facility = new Facility();
        facility.setId(id);
        facility.setName(name);
        return facility;
    }

    private UserAdmin createMockUserAdmin() {
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("admin-1");
        userAdmin.setCode("AD001");
        userAdmin.setName("Admin User");
        userAdmin.setEmail("admin@fpt.edu.vn");
        return userAdmin;
    }

    private UserStaff createMockUserStaff() {
        UserStaff userStaff = new UserStaff();
        userStaff.setId("staff-1");
        userStaff.setCode("ST001");
        userStaff.setName("Staff User");
        userStaff.setEmailFpt("staff@fpt.edu.vn");
        return userStaff;
    }

    private UserStudent createMockUserStudent() {
        UserStudent userStudent = new UserStudent();
        userStudent.setId("student-1");
        userStudent.setCode("SV001");
        userStudent.setName("Student User");
        userStudent.setEmail("student@fpt.edu.vn");
        return userStudent;
    }

    private AuthUser createMockAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setId("user-1");
        authUser.setName("Test User");
        authUser.setCode("TEST001");
        authUser.setEmail("test@fpt.edu.vn");
        authUser.setRole(Set.of(RoleConstant.ADMIN));
        return authUser;
    }

    private AuthenticationStudentRegisterRequest createMockStudentRegisterRequest() {
        AuthenticationStudentRegisterRequest request = new AuthenticationStudentRegisterRequest();
        request.setIdFacility("facility-1");
        request.setCode("SV001");
        request.setName("Test Student");
        return request;
    }
}
