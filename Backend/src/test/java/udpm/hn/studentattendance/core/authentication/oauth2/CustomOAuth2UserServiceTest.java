package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomOAuth2UserServiceTest {
    @Mock
    private HttpSession httpSession;
    @Mock
    private AuthenticationUserAdminRepository authenticationUserAdminRepository;
    @Mock
    private AuthenticationUserStaffRepository authenticationUserStaffRepository;
    @Mock
    private AuthenticationUserStudentRepository authenticationUserStudentRepository;
    @Mock
    private AuthenticationRoleRepository authenticationRoleRepository;
    @Mock
    private SettingHelper settingHelper;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUser() {
        // Mock OAuth2UserRequest with proper ClientRegistration
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        ProviderDetails providerDetails = mock(ProviderDetails.class);
        ClientRegistration.ProviderDetails.UserInfoEndpoint userInfoEndpoint = mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(userInfoEndpoint);
        when(userInfoEndpoint.getUri()).thenReturn("https://www.googleapis.com/oauth2/v3/userinfo");

        // Mock OAuth2User
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("http://example.com/picture.jpg");
        when(oauth2User.getAttribute("sub")).thenReturn("12345");

        // Mock session attributes
        when(httpSession.getAttribute("LOGIN_ROLE")).thenReturn("STUDENT");
        when(httpSession.getAttribute("LOGIN_FACILITY")).thenReturn("facility-1");

        // Mock repository responses
        when(authenticationUserStudentRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, Boolean.class)).thenReturn(false);

        // Test the method
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
    }
}