package udpm.hn.studentattendance.core.authentication.oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2FailureHandlerTest {

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private CustomOAuth2FailureHandler failureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testOnAuthenticationFailure() throws ServletException, IOException {
        // Arrange
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");
        when(httpSession.getAttribute("login_redirect")).thenReturn("http://localhost:3000");

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl().contains("localhost:3000"));
    }

    @Test
    void testOnAuthenticationFailureWithNullException() throws ServletException, IOException {
        // Arrange
        AuthenticationException exception = null;
        when(httpSession.getAttribute("login_redirect")).thenReturn("http://localhost:3000");

        // Act & Assert - should throw exception due to null pointer
        assertThrows(NullPointerException.class, () -> {
            failureHandler.onAuthenticationFailure(request, response, exception);
        });
    }
}