package udpm.hn.studentattendance.core.authentication.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void testAuthenticationControllerExists() {
        assertNotNull(authenticationController);
    }

    @Test
    void testLogin() throws Exception {
        String role = "student";
        String redirectUri = "http://example.com";
        String facilityId = "123";

        RedirectView expectedRedirect = new RedirectView("http://example.com");
        when(authenticationService.authorSwitch(role, redirectUri, facilityId)).thenReturn(expectedRedirect);

        RedirectView response = authenticationController.login(role, redirectUri, facilityId);

        assertNotNull(response);
        assertEquals(expectedRedirect, response);
        verify(authenticationService).authorSwitch(role, redirectUri, facilityId);
    }

    @Test
    void testLoginWithNullParameters() throws Exception {
        RedirectView expectedRedirect = new RedirectView("http://example.com");
        when(authenticationService.authorSwitch(null, null, null)).thenReturn(expectedRedirect);

        RedirectView response = authenticationController.login(null, null, null);

        assertNotNull(response);
        assertEquals(expectedRedirect, response);
        verify(authenticationService).authorSwitch(null, null, null);
    }
}
