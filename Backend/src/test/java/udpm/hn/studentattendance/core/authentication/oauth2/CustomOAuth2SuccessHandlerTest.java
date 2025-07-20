package udpm.hn.studentattendance.core.authentication.oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2SuccessHandlerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private CustomOAuth2SuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testOnAuthenticationSuccess() throws ServletException, IOException {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@fpt.edu.vn");

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oauth2User);
        when(authentication.getPrincipal()).thenReturn(customOAuth2User);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(oauth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");
        when(httpSession.getAttribute("login_role")).thenReturn("ADMIN");
        when(httpSession.getAttribute("login_redirect")).thenReturn("http://localhost:3000");
        when(jwtUtil.generateToken(anyString(), any(CustomOAuth2User.class))).thenReturn("access.token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh.token");

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl().contains("localhost:3000"));
    }

    @Test
    void testOnAuthenticationSuccessWithNullPrincipal() throws ServletException, IOException {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert - should throw exception due to null pointer
        assertThrows(NullPointerException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });
    }

    @Test
    void testOnAuthenticationSuccessWithNonOAuth2User() throws ServletException, IOException {
        // Arrange
        Object nonOAuth2User = new Object();
        when(authentication.getPrincipal()).thenReturn(nonOAuth2User);

        // Act & Assert - should throw exception due to class cast
        assertThrows(ClassCastException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });
    }
}
