package udpm.hn.studentattendance.infrastructure.config.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisTemplateConfigTest {
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void testClassExists() {
        assertNotNull(new RedisTemplateConfig(objectMapper));
    }
}
