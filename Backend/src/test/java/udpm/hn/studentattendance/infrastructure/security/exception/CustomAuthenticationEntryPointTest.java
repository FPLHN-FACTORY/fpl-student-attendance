package udpm.hn.studentattendance.infrastructure.security.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {
    @Mock
    private AuthenticationException authenticationException;

    @Test
    void testCommence() throws Exception {
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, authenticationException);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Truy cập bị từ chối"));
    }

    @Test
    void testEntryPointExists() {
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        assertNotNull(entryPoint);
    }
}
