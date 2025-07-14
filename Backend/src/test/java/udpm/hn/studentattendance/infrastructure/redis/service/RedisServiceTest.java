package udpm.hn.studentattendance.infrastructure.redis.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RedisServiceTest {
    @Test
    void testSetAndGetObject() {
        RedisService redis = mock(RedisService.class);
        when(redis.getObject("key", String.class)).thenReturn("value");
        redis.setObject("key", "value");
        String result = redis.getObject("key", String.class);
        assertEquals("value", result);
        verify(redis).setObject("key", "value");
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