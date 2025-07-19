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
import com.fasterxml.jackson.core.type.TypeReference;
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
        // Sử dụng lenient để tránh lỗi unnecessary stubbings
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
        // Manually initialize hashOperations since @PostConstruct might not be called
        // in tests
        try {
            java.lang.reflect.Field hashOperationsField = RedisServiceImpl.class.getDeclaredField("hashOperations");
            hashOperationsField.setAccessible(true);
            hashOperationsField.set(redisService, hashOps);
        } catch (Exception e) {
            // If reflection fails, the test will still work if @PostConstruct is called
        }
    }

    @Test
    void testGetObject() throws Exception {
        String expectedValue = "value";
        when(valueOps.get("key")).thenReturn(expectedValue);
        when(objectMapper.readValue(eq(expectedValue), any(TypeReference.class))).thenReturn(expectedValue);
        TypeReference<String> typeRef = new TypeReference<String>() {
        };
        Object result = redisService.getObject("key", typeRef);
        assertEquals(expectedValue, result);
        verify(valueOps).get("key");
        verify(objectMapper).readValue(eq(expectedValue), eq(typeRef));
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
        verify(hashOps).hasKey("key", "field");
    }
}