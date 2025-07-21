package udpm.hn.studentattendance.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FrontendControllerTest {
    @InjectMocks
    private FrontendController frontendController;

    @Test
    void testFrontendControllerExists() {
        assertNotNull(frontendController);
    }

    @Test
    void testForward() {
        String result = frontendController.forward();
        assertEquals("forward:/index.html", result);
    }
}
