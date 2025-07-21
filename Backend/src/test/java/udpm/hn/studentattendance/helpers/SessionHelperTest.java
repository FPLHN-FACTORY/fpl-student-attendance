package udpm.hn.studentattendance.helpers;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionHelperTest {

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private SessionHelper sessionHelper;

    @BeforeEach
    void setUp() {
        // Reset static fields before each test
        resetStaticFields();
    }

    private void resetStaticFields() {
        // Reset static fields using reflection or create new instance
        // This is a simplified approach - in real implementation you might need
        // reflection
    }

    @Test
    void canInstantiate() {
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        SessionHelper helper = new SessionHelper(mockSession);
        assertThat(helper).isNotNull();
    }

    @Test
    void testGetCurrentUser() {
        // Given
        AuthUser mockAuthUser = new AuthUser();
        mockAuthUser.setId("user123");
        mockAuthUser.setName("Test User");

        when(httpSession.getAttribute(SessionConstant.AUTH_USER)).thenReturn(mockAuthUser);

        // When
        AuthUser result = sessionHelper.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("Test User", result.getName());
        verify(httpSession).getAttribute(SessionConstant.AUTH_USER);
    }

    @Test
    void testGetCurrentUserWithNull() {
        // Given
        when(httpSession.getAttribute(SessionConstant.AUTH_USER)).thenReturn(null);

        // When
        AuthUser result = sessionHelper.getCurrentUser();

        // Then
        assertNull(result);
        verify(httpSession).getAttribute(SessionConstant.AUTH_USER);
    }

    @Test
    void testSetCurrentUser() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setId("user123");
        authUser.setName("Test User");

        // When
        sessionHelper.setCurrentUser(authUser);

        // Then
        verify(httpSession).setAttribute(eq(SessionConstant.AUTH_USER), eq(authUser));
    }

    @Test
    void testBuildAuthUserFromUserAdmin() {
        // Given
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("admin123");
        userAdmin.setCode("ADMIN001");
        userAdmin.setName("Admin User");
        userAdmin.setEmail("admin@example.com");
        userAdmin.setImage("admin.jpg");

        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.ADMIN);

        String facilityId = "facility123";

        // When
        AuthUser result = sessionHelper.buildAuthUser(userAdmin, roles, facilityId);

        // Then
        assertNotNull(result);
        assertEquals("admin123", result.getId());
        assertEquals("ADMIN001", result.getCode());
        assertEquals("Admin User", result.getName());
        assertEquals("admin@example.com", result.getEmail());
        assertEquals(roles, result.getRole());
        assertEquals(facilityId, result.getIdFacility());
        assertEquals("admin.jpg", result.getPicture());
    }

    @Test
    void testBuildAuthUserFromUserStaff() {
        // Given
        UserStaff userStaff = new UserStaff();
        userStaff.setId("staff123");
        userStaff.setCode("STAFF001");
        userStaff.setName("Staff User");
        userStaff.setEmailFe("staff.fe@example.com");
        userStaff.setEmailFpt("staff.fpt@example.com");
        userStaff.setImage("staff.jpg");

        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.STAFF);

        String facilityId = "facility123";

        // When
        AuthUser result = sessionHelper.buildAuthUser(userStaff, roles, facilityId);

        // Then
        assertNotNull(result);
        assertEquals("staff123", result.getId());
        assertEquals("STAFF001", result.getCode());
        assertEquals("Staff User", result.getName());
        assertEquals("staff.fpt@example.com", result.getEmail());
        assertEquals("staff.fe@example.com", result.getEmailFe());
        assertEquals("staff.fpt@example.com", result.getEmailFpt());
        assertEquals(roles, result.getRole());
        assertEquals(facilityId, result.getIdFacility());
        assertEquals("staff.jpg", result.getPicture());
    }

    @Test
    void testBuildAuthUserFromUserStudent() {
        // Given
        UserStudent userStudent = new UserStudent();
        userStudent.setId("student123");
        userStudent.setCode("STUDENT001");
        userStudent.setName("Student User");
        userStudent.setEmail("student@example.com");
        userStudent.setImage("student.jpg");

        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.STUDENT);

        String facilityId = "facility123";

        // When
        AuthUser result = sessionHelper.buildAuthUser(userStudent, roles, facilityId);

        // Then
        assertNotNull(result);
        assertEquals("student123", result.getId());
        assertEquals("STUDENT001", result.getCode());
        assertEquals("Student User", result.getName());
        assertEquals("student@example.com", result.getEmail());
        assertEquals(roles, result.getRole());
        assertEquals(facilityId, result.getIdFacility());
        assertEquals("student.jpg", result.getPicture());
    }

    @Test
    void testBuildAuthUserWithNullValues() {
        // Given
        UserAdmin userAdmin = new UserAdmin();
        // Don't set any values

        Set<RoleConstant> roles = new HashSet<>();

        // When
        AuthUser result = sessionHelper.buildAuthUser(userAdmin, roles, null);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getCode());
        assertNull(result.getName());
        assertNull(result.getEmail());
        assertEquals(roles, result.getRole());
        assertNull(result.getIdFacility());
        assertNull(result.getPicture());
    }

    @Test
    void testGetUserId() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setId("user123");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserId();

        // Then
        assertEquals("user123", result);
    }

    @Test
    void testGetUserName() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setName("Test User");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserName();

        // Then
        assertEquals("Test User", result);
    }

    @Test
    void testGetUserRole() {
        // Given
        AuthUser authUser = new AuthUser();
        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.ADMIN);
        authUser.setRole(roles);
        sessionHelper.setCurrentUser(authUser);

        // When
        Set<RoleConstant> result = sessionHelper.getUserRole();

        // Then
        assertEquals(roles, result);
    }

    @Test
    void testGetFacilityId() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setIdFacility("facility123");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getFacilityId();

        // Then
        assertEquals("facility123", result);
    }

    @Test
    void testGetUserEmail() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setEmail("user@example.com");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserEmail();

        // Then
        assertEquals("user@example.com", result);
    }

    @Test
    void testGetUserEmailFe() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setEmailFe("user.fe@example.com");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserEmailFe();

        // Then
        assertEquals("user.fe@example.com", result);
    }

    @Test
    void testGetUserEmailFpt() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setEmailFpt("user.fpt@example.com");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserEmailFpt();

        // Then
        assertEquals("user.fpt@example.com", result);
    }

    @Test
    void testGetUserPicture() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setPicture("user.jpg");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserPicture();

        // Then
        assertEquals("user.jpg", result);
    }

    @Test
    void testGetUserCode() {
        // Given
        AuthUser authUser = new AuthUser();
        authUser.setCode("USER001");
        sessionHelper.setCurrentUser(authUser);

        // When
        String result = sessionHelper.getUserCode();

        // Then
        assertEquals("USER001", result);
    }

    @Test
    void testGetLoginRole() {
        // Given
        sessionHelper.setLoginRole(RoleConstant.ADMIN);

        // When
        RoleConstant result = sessionHelper.getLoginRole();

        // Then
        assertEquals(RoleConstant.ADMIN, result);
    }

    @Test
    void testSetLoginRole() {
        // Given
        RoleConstant role = RoleConstant.STAFF;

        // When
        sessionHelper.setLoginRole(role);

        // Then
        assertEquals(role, sessionHelper.getLoginRole());
    }

    @Test
    void testBuildAuthUserWithMultipleRoles() {
        // Given
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("user123");
        userAdmin.setName("Multi Role User");

        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.ADMIN);
        roles.add(RoleConstant.STAFF);

        // When
        AuthUser result = sessionHelper.buildAuthUser(userAdmin, roles, "facility123");

        // Then
        assertNotNull(result);
        assertEquals(roles, result.getRole());
    }

    @Test
    void testBuildAuthUserWithEmptyRoles() {
        // Given
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId("user123");
        userAdmin.setName("No Role User");

        Set<RoleConstant> roles = new HashSet<>();

        // When
        AuthUser result = sessionHelper.buildAuthUser(userAdmin, roles, "facility123");

        // Then
        assertNotNull(result);
        assertEquals(roles, result.getRole());
    }
}
