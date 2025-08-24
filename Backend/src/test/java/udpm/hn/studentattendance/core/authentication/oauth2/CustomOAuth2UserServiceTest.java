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
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        CustomOAuth2User user = (CustomOAuth2User) spyService.loadUser(userRequest);
        assertEquals("admin-1", user.getId());
        assertEquals("A001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.ADMIN));
    }

    @Test
    void testLoadUser_AdminNotFound() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("ADMIN");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> spyService.loadUser(userRequest));
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
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        CustomOAuth2User user = (CustomOAuth2User) spyService.loadUser(userRequest);
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
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> spyService.loadUser(userRequest));
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
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        CustomOAuth2User user = (CustomOAuth2User) spyService.loadUser(userRequest);
        assertEquals("stu-1", user.getId());
        assertEquals("STU001", user.getCode());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_CreateNew() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(settingHelper.getSetting(eq(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT), eq(Boolean.class)))
                .thenReturn(false);
        when(studentRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        CustomOAuth2User user = (CustomOAuth2User) spyService.loadUser(userRequest);
        assertEquals("test@fpt.edu.vn", user.getEmail());
        assertTrue(user.getRole().contains(RoleConstant.STUDENT));
    }

    @Test
    void testLoadUser_StudentNotFound_InvalidEmail() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(settingHelper.getSetting(eq(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT), eq(Boolean.class)))
                .thenReturn(false);
        // Email không hợp lệ
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "notfpt@gmail.com");
        attributes.put("name", "Test User");
        attributes.put("picture", "pic.png");
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2User.getAttribute("email")).thenReturn("notfpt@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn("pic.png");
        CustomOAuth2UserService spyService = spy(service);
        doReturn(new CustomOAuth2User(oAuth2User)).when((DefaultOAuth2UserService) spyService).loadUser(any());
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> spyService.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_StudentFacilityMismatch() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("STUDENT");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        UserStudent student = new UserStudent();
        student.setId("stu-1");
        student.setCode("STU001");
        Facility facility = new Facility();
        facility.setId("facility-2");
        student.setFacility(facility);
        when(studentRepo.findByEmail(anyString())).thenReturn(Optional.of(student));
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> spyService.loadUser(userRequest));
        assertEquals("login_failed", ex.getError().getErrorCode());
    }

    @Test
    void testLoadUser_InvalidRole() {
        when(httpSession.getAttribute(SessionConstant.LOGIN_ROLE)).thenReturn("INVALID_ROLE");
        when(httpSession.getAttribute(SessionConstant.LOGIN_FACILITY)).thenReturn("facility-1");
        CustomOAuth2UserService spyService = spy(service);
        doReturn(getCustomOAuth2User()).when((DefaultOAuth2UserService) spyService).loadUser(any());
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> spyService.loadUser(userRequest));
        assertEquals("invalid_role", ex.getError().getErrorCode());
    }
}
