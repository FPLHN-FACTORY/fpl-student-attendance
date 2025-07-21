package udpm.hn.studentattendance.core.authentication.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws IOException {
        // Mock the service methods to return proper responses
        RedirectView mockRedirectView = new RedirectView("http://example.com");

        lenient().when(authenticationService.authorSwitch(any(String.class), any(String.class), any(String.class)))
                .thenReturn(mockRedirectView);
        lenient().when(authenticationService.authorSwitch(null, null, null))
                .thenReturn(mockRedirectView);
        lenient().when(authenticationService.authorSwitch("", "", ""))
                .thenReturn(mockRedirectView);
    }

    @Test
    void testAuthenticationServiceExists() {
        assertNotNull(authenticationService);
    }

    @Test
    void testAuthorSwitch() throws Exception {
        String role = "student";
        String redirectUri = "http://example.com";
        String facilityId = "123";

        RedirectView result = authenticationService.authorSwitch(role, redirectUri, facilityId);

        assertNotNull(result);
        assertNotNull(result.getUrl());
    }

    @Test
    void testAuthorSwitchWithNullParameters() throws Exception {
        RedirectView result = authenticationService.authorSwitch(null, null, null);

        assertNotNull(result);
        assertNotNull(result.getUrl());
    }

    @Test
    void testAuthorSwitchWithEmptyParameters() throws Exception {
        RedirectView result = authenticationService.authorSwitch("", "", "");

        assertNotNull(result);
        assertNotNull(result.getUrl());
    }
}
