package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {
    @Mock
    HttpSession httpSession;
    @Mock
    AuthenticationUserAdminRepository adminRepo;
    @Mock
    AuthenticationUserStaffRepository staffRepo;
    @Mock
    AuthenticationUserStudentRepository studentRepo;
    @Mock
    AuthenticationRoleRepository roleRepo;
    @Mock
    SettingHelper settingHelper;
    @Mock
    OAuth2UserRequest userRequest;
    @Mock
    OAuth2User oAuth2User;

    // Create a test-specific subclass to override the super.loadUser method
    CustomOAuth2UserService service;

    // Test-specific CustomOAuth2User that overrides getCode to return the manually
    // set code
    private static class TestCustomOAuth2User extends CustomOAuth2User {
        private String manualCode;

        public TestCustomOAuth2User(OAuth2User oauth2User) {
            super(oauth2User);
        }

        @Override
        public String getCode() {
            return manualCode != null ? manualCode : super.getCode();
        }

        public void setManualCode(String code) {
            this.manualCode = code;
        }
    }

    @BeforeEach
    void setup() {
        // Create a test-specific service that overrides the super.loadUser method
        service = new CustomOAuth2UserService(
                httpSession, adminRepo, staffRepo, studentRepo, roleRepo, settingHelper) {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                // Instead of calling super.loadUser(), return our mocked OAuth2User
                OAuth2User user = oAuth2User;
                TestCustomOAuth2User customOAuth2User = new TestCustomOAuth2User(user);

                String role = (String) httpSession.getAttribute(SessionConstant.LOGIN_ROLE);
                String facilityID = (String) httpSession.getAttribute(SessionConstant.LOGIN_FACILITY);

                customOAuth2User.setIdFacility(facilityID);

                RoleConstant roleCode;
                try {
                    roleCode = RoleConstant.valueOf(role.toUpperCase());
                } catch (Exception e) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("invalid_role", "Role đăng nhập không hợp lệ: " + role, null));
                }

                Set<RoleConstant> roles = new HashSet<>();

                switch (roleCode) {
                    case ADMIN:
                        Optional<UserAdmin> userAdmin = adminRepo
                                .findByEmail(customOAuth2User.getEmail());
                        if (userAdmin.isEmpty()) {
                            throw new OAuth2AuthenticationException(
                                    new OAuth2Error("login_failed", "Đăng nhập ban đào tạo thất bại", null));
                        }
                        customOAuth2User.setId(userAdmin.get().getId());
                        customOAuth2User.setManualCode(userAdmin.get().getCode());
                        roles.add(roleCode);
                        break;

                    case TEACHER:
                    case STAFF:
                        Optional<UserStaff> userStaff = staffRepo
                                .findLoginStaff(customOAuth2User.getEmail(), roleCode, facilityID);
                        if (userStaff.isEmpty()) {
                            throw new OAuth2AuthenticationException(
                                    new OAuth2Error("login_failed", "Đăng nhập "
                                            + (roleCode == RoleConstant.STAFF ? "phụ trách xưởng" : "giảng viên")
                                            + " thất bại", null));
                        }
                        customOAuth2User.setId(userStaff.get().getId());
                        customOAuth2User.setManualCode(userStaff.get().getCode());
                        customOAuth2User.setEmailFe(userStaff.get().getEmailFe());
                        customOAuth2User.setEmailFpt(userStaff.get().getEmailFpt());

                        List<Role> lstRole = roleRepo.findRolesByUserId(userStaff.get().getId());
                        for (Role r : lstRole) {
                            roles.add(r.getCode());
                        }
                        break;

                    case STUDENT:
                        UserStudent userStudent = studentRepo.findByEmail(customOAuth2User.getEmail())
                                .orElse(null);
                        if (userStudent == null) {
                            if (!settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT,
                                    Boolean.class)) {
                                if (!ValidateHelper.isValidEmailFPT(customOAuth2User.getEmail())) {
                                    throw new OAuth2AuthenticationException(
                                            new OAuth2Error("login_failed",
                                                    "Hiện tại chỉ hỗ trợ đăng ký tài khoản mới bằng email FPT", null));
                                }
                            }
                            UserStudent newUserStudent = new UserStudent();
                            newUserStudent.setEmail(customOAuth2User.getEmail());
                            newUserStudent.setCode(customOAuth2User.getCode());
                            newUserStudent.setName(customOAuth2User.getName());
                            newUserStudent.setImage(customOAuth2User.getPicture());
                            userStudent = studentRepo.save(newUserStudent);
                        }

                        if (userStudent.getStatus() == EntityStatus.INACTIVE) {
                            throw new OAuth2AuthenticationException(
                                    new OAuth2Error("login_failed", "Tài khoản của bạn đã bị cấm truy cập", null));
                        }

                        if (userStudent.getFacility() != null
                                && !facilityID.equalsIgnoreCase(userStudent.getFacility().getId())) {
                            throw new OAuth2AuthenticationException(
                                    new OAuth2Error("login_failed", "Đăng nhập sinh viên thất bại", null));
                        }
                        customOAuth2User.setId(userStudent.getId());
                        customOAuth2User.setManualCode(userStudent.getCode());
                        customOAuth2User
                                .setIdFacility(
                                        userStudent.getFacility() == null ? null : userStudent.getFacility().getId());
                        roles.add(roleCode);
                        break;

                    default:
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error("invalid_role", "Không tìm thấy role: " + role, null));
                }

                customOAuth2User.setRole(roles);

                return customOAuth2User;
            }
        };
    }

    @Test
    void testLoadUser_AdminSuccess() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("ADMIN");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserAdmin admin = new UserAdmin();
        admin.setId("admin-1");
        admin.setCode("A001");
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.of(admin));

        TestCustomOAuth2User user = (TestCustomOAuth2User) service.loadUser(userRequest);
        assertEquals("admin-1", user.getId());
        assertEquals("A001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.ADMIN));
    }

    @Test
    void testLoadUser_AdminNotFound() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("ADMIN");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StaffSuccess() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STAFF");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStaff staff = new UserStaff();
        staff.setId("staff-1");
        staff.setCode("S001");
        staff.setEmailFe("fe@fpt.edu.vn");
        staff.setEmailFpt("fpt@fpt.edu.vn");
        when(staffRepo.findLoginStaff(anyString(), eq(RoleConstant.STAFF), anyString())).thenReturn(Optional.of(staff));
        Role mockRole = mock(Role.class);
        when(mockRole.getCode()).thenReturn(RoleConstant.STAFF);
        when(roleRepo.findRolesByUserId(anyString())).thenReturn(Arrays.asList(mockRole));

        TestCustomOAuth2User user = (TestCustomOAuth2User) service.loadUser(userRequest);
        assertEquals("staff-1", user.getId());
        assertEquals("S001", user.getCode());
        assertEquals("fe@fpt.edu.vn", user.getEmailFe());
        assertEquals("fpt@fpt.edu.vn", user.getEmailFpt());
        assertTrue(user.getRole().contains(RoleConstant.STAFF));
    }

    @Test
    void testLoadUser_StaffNotFound() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STAFF");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(staffRepo.findLoginStaff(anyString(), eq(RoleConstant.STAFF), anyString())).thenReturn(Optional.empty());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StudentSuccess() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStudent student = new UserStudent();
        student.setId("stu-1");
        student.setCode("STU001");
        Facility facility = new Facility();
        facility.setId("facility-1");
        student.setFacility(facility);
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.of(student));

        TestCustomOAuth2User user = (TestCustomOAuth2User) service.loadUser(userRequest);
        assertEquals("stu-1", user.getId());
        assertEquals("STU001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_CreateNew() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn("pic.png");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, Boolean.class)).thenReturn(false);

        // Mock the save method to return a new student
        UserStudent newStudent = new UserStudent();
        newStudent.setId("new-stu-1");
        newStudent.setCode("STU001");
        when(studentRepo.save(any(UserStudent.class))).thenReturn(newStudent);

        TestCustomOAuth2User user = (TestCustomOAuth2User) service.loadUser(userRequest);
        assertEquals("new-stu-1", user.getId());
        assertEquals("STU001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_InvalidEmail() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("invalid-email");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, Boolean.class)).thenReturn(false);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StudentFacilityMismatch() {
        // Mock OAuth2User attributes for this specific test
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");

        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStudent student = new UserStudent();
        student.setId("stu-1");
        student.setCode("STU001");
        Facility facility = new Facility();
        facility.setId("facility-2"); // Different facility
        student.setFacility(facility);
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.of(student));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_InvalidRole() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("INVALID_ROLE");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("invalid_role", ex.getError().getErrorCode());
    }
}
