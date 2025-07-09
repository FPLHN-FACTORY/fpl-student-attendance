package udpm.hn.studentattendance.infrastructure.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import udpm.hn.studentattendance.infrastructure.redis.service.impl.RedisServiceImpl;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void testSetObject() {
        redisService.setObject("key", "value");
        verify(valueOps).set(eq("key"), eq("value"), eq(3L), eq(java.util.concurrent.TimeUnit.DAYS));
    }

    @Test
    void testGetObject() throws Exception {
        when(valueOps.get("key")).thenReturn("value");
        when(objectMapper.readValue("value", String.class)).thenReturn("value");
        Object result = redisService.getObject("key", String.class);
        assertEquals("value", result);
        verify(valueOps).get("key");
    }

    @Test
    void testDelete() {
        redisService.delete("key");
        verify(redisTemplate).delete("key");
    }

    @Test
    void testHashExist() {
        when(hashOps.hasKey("key", "field")).thenReturn(true);
        boolean result = redisService.hashExist("key", "field");
        assertTrue(result);
        verify(hashOps).hasKey("key", "field");
    }

    @Test
    void testHashExistFalse() {
        when(hashOps.hasKey("key", "field")).thenReturn(false);
        boolean result = redisService.hashExist("key", "field");
        assertFalse(result);
    }
}