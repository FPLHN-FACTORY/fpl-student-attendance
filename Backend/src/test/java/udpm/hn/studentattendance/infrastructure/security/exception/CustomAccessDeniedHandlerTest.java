package udpm.hn.studentattendance.infrastructure.security.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {
    @Mock
    private AccessDeniedException accessDeniedException;

    @Test
    void testHandleAccessDenied() throws Exception {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, accessDeniedException);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Truy cập bị từ chối"));
    }

    @Test
    void testHandlerExists() {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        assertNotNull(handler);
    }
}
