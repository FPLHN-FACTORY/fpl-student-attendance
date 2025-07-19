package udpm.hn.studentattendance.infrastructure.redis.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.type.TypeReference;

class RedisServiceTest {
    @Test
    void testSetAndGetObject() {
        RedisService redis = mock(RedisService.class);
        TypeReference<String> typeRef = new TypeReference<String>() {
        };
        when(redis.getObject("key", typeRef)).thenReturn("value");
        String result = redis.getObject("key", typeRef);
        assertEquals("value", result);
    }

    @Test
    void testDeleteAndExist() {
        RedisService redis = mock(RedisService.class);
        when(redis.hashExist("k", "f")).thenReturn(true);
        assertTrue(redis.hashExist("k", "f"));
        redis.delete("k");
        verify(redis).delete("k");
    }
}