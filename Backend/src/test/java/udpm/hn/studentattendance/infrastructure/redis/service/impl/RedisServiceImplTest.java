package udpm.hn.studentattendance.infrastructure.redis.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import udpm.hn.studentattendance.infrastructure.redis.service.impl.RedisServiceImpl;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisServiceImplTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSetAndGet() {
        String key = "testKey";
        String value = "testValue";
        doNothing().when(valueOperations).set(key, value, 1000L, TimeUnit.SECONDS);
        when(valueOperations.get(key)).thenReturn(value);

        redisService.set(key, value, 1000L);
        Object result = redisService.get(key);
        assertEquals(value, result);
    }

    @Test
    void testDelete() {
        String key = "testKey";
        when(redisTemplate.delete(key)).thenReturn(true);
        redisService.delete(key);
        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    void testGetObject() {
        String key = "objKey";
        DummyObject dummy = new DummyObject("abc");
        when(valueOperations.get(key)).thenReturn(dummy);
        when(objectMapper.convertValue(dummy, DummyObject.class)).thenReturn(dummy);

        DummyObject result = redisService.getObject(key, DummyObject.class);
        assertNotNull(result);
        assertEquals("abc", result.value);
    }

    @Test
    void testSetObject() {
        String key = "objKey";
        DummyObject dummy = new DummyObject("abc");
        doNothing().when(valueOperations).set(key, dummy, 1000L, TimeUnit.SECONDS);
        redisService.set(key, dummy, 1000L);
        verify(valueOperations, times(1)).set(key, dummy, 1000L, TimeUnit.SECONDS);
    }

    static class DummyObject implements java.io.Serializable {
        String value;

        DummyObject(String value) {
            this.value = value;
        }
    }
}