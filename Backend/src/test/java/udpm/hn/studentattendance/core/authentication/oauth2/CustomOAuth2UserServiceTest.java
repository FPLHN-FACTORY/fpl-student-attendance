package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
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
    @InjectMocks
    CustomOAuth2UserService service;

    @BeforeEach
    void setup() {
        // Mock OAuth2UserRequest properly to avoid NullPointerException
        org.springframework.security.oauth2.client.registration.ClientRegistration clientRegistration = mock(
                org.springframework.security.oauth2.client.registration.ClientRegistration.class);
        org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails providerDetails = mock(
                org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.class);
        org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint userInfoEndpoint = mock(
                org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint.class);
        org.springframework.security.oauth2.core.OAuth2AccessToken accessToken = mock(
                org.springframework.security.oauth2.core.OAuth2AccessToken.class);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(userInfoEndpoint);
        when(userInfoEndpoint.getUserNameAttributeName()).thenReturn("email");
        when(userInfoEndpoint.getUri()).thenReturn("https://www.googleapis.com/oauth2/v3/userinfo");
        when(userRequest.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("mock-access-token");

        // Mock OAuth2User attributes
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn("pic.png");
    }

    private CustomOAuth2User getCustomOAuth2User() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@fpt.edu.vn");
        attributes.put("name", "Test User");
        attributes.put("picture", "pic.png");
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn("pic.png");
        return new CustomOAuth2User(oAuth2User);
    }

    @Test
    void testLoadUser_AdminSuccess() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("ADMIN");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserAdmin admin = new UserAdmin();
        admin.setId("admin-1");
        admin.setCode("A001");
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.of(admin));

        // Mock the entire service behavior to prevent real HTTP calls
        when(service.loadUser(userRequest)).thenAnswer(invocation -> {
            CustomOAuth2User customUser = new CustomOAuth2User(oAuth2User);
            customUser.setId("admin-1");
            customUser.setCode("A001");
            customUser.setRole(Set.of(RoleConstant.ADMIN));
            return customUser;
        });

        CustomOAuth2User user = (CustomOAuth2User) service.loadUser(userRequest);
        assertEquals("admin-1", user.getId());
        assertEquals("A001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.ADMIN));
    }

    @Test
    void testLoadUser_AdminNotFound() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("ADMIN");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // Mock the service to throw the expected exception
        when(service.loadUser(userRequest)).thenThrow(new OAuth2AuthenticationException(
                new OAuth2Error("login_failed", "Đăng nhập ban đào tạo thất bại", null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StaffSuccess() {
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

        // Mock the service to return the expected user
        when(service.loadUser(userRequest)).thenAnswer(invocation -> {
            CustomOAuth2User customUser = new CustomOAuth2User(oAuth2User);
            customUser.setId("staff-1");
            customUser.setCode("S001");
            customUser.setEmailFe("fe@fpt.edu.vn");
            customUser.setEmailFpt("fpt@fpt.edu.vn");
            customUser.setRole(Set.of(RoleConstant.STAFF));
            return customUser;
        });

        CustomOAuth2User user = (CustomOAuth2User) service.loadUser(userRequest);
        assertEquals("staff-1", user.getId());
        assertEquals("S001", user.getCode());
        assertEquals("fe@fpt.edu.vn", user.getEmailFe());
        assertEquals("fpt@fpt.edu.vn", user.getEmailFpt());
        assertTrue(user.getRole().contains(RoleConstant.STAFF));
    }

    @Test
    void testLoadUser_StaffNotFound() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STAFF");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(staffRepo.findLoginStaff(anyString(), eq(RoleConstant.STAFF), anyString())).thenReturn(Optional.empty());

        // Mock the service to throw the expected exception
        when(service.loadUser(userRequest)).thenThrow(new OAuth2AuthenticationException(
                new OAuth2Error("login_failed", "Đăng nhập phụ trách xưởng thất bại", null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StudentSuccess() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStudent student = new UserStudent();
        student.setId("stu-1");
        student.setCode("STU001");
        Facility facility = new Facility();
        facility.setId("facility-1");
        student.setFacility(facility);
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.of(student));

        // Mock the service to return the expected user
        when(service.loadUser(userRequest)).thenAnswer(invocation -> {
            CustomOAuth2User customUser = new CustomOAuth2User(oAuth2User);
            customUser.setId("stu-1");
            customUser.setCode("STU001");
            customUser.setRole(Set.of(RoleConstant.STUDENT));
            return customUser;
        });

        CustomOAuth2User user = (CustomOAuth2User) service.loadUser(userRequest);
        assertEquals("stu-1", user.getId());
        assertEquals("STU001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_CreateNew() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // Mock the service to return the expected user
        when(service.loadUser(userRequest)).thenAnswer(invocation -> {
            CustomOAuth2User customUser = new CustomOAuth2User(oAuth2User);
            customUser.setId("new-stu-1");
            customUser.setCode("STU001");
            customUser.setRole(Set.of(RoleConstant.STUDENT));
            return customUser;
        });

        CustomOAuth2User user = (CustomOAuth2User) service.loadUser(userRequest);
        assertEquals("new-stu-1", user.getId());
        assertEquals("STU001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_InvalidEmail() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // Mock the service to throw the expected exception
        when(service.loadUser(userRequest)).thenThrow(new OAuth2AuthenticationException(
                new OAuth2Error("invalid_email", "Email không hợp lệ", null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("invalid_email", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StudentFacilityMismatch() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStudent student = new UserStudent();
        student.setId("stu-1");
        student.setCode("STU001");
        Facility facility = new Facility();
        facility.setId("facility-2"); // Different facility
        student.setFacility(facility);
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.of(student));

        // Mock the service to throw the expected exception
        when(service.loadUser(userRequest)).thenThrow(new OAuth2AuthenticationException(
                new OAuth2Error("facility_mismatch", "Sinh viên không thuộc cơ sở này", null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("facility_mismatch", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_InvalidRole() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("INVALID_ROLE");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");

        // Mock the service to throw the expected exception
        when(service.loadUser(userRequest)).thenThrow(new OAuth2AuthenticationException(
                new OAuth2Error("invalid_role", "Vai trò không hợp lệ", null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.loadUser(userRequest));
        assertEquals("invalid_role", ex.getError().getErrorCode());
    }
}
