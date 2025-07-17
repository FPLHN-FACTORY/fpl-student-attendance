package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import udpm.hn.studentattendance.infrastructure.config.redis.service.QueryCacheService;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisStartupCleanerHelperTest {
    @Test
    void canInstantiate() {
        QueryCacheService mockQueryCache = Mockito.mock(QueryCacheService.class);
        RedisService mockRedis = Mockito.mock(RedisService.class);
        RedisStartupCleanerHelper helper = new RedisStartupCleanerHelper(mockQueryCache, mockRedis);
        assertThat(helper).isNotNull();
    }

    @Test
    void testCleanRedisOnStartupSuccess() {
        QueryCacheService mockQueryCache = mock(QueryCacheService.class);
        RedisService mockRedis = mock(RedisService.class);
        RedisStartupCleanerHelper helper = new RedisStartupCleanerHelper(mockQueryCache, mockRedis);
        helper.cleanRedisOnStartup();
        verify(mockQueryCache).clearAllCaches();
        verify(mockRedis).deletePattern("*");
    }

    @Test
    void testCleanRedisOnStartupWithException() {
        QueryCacheService mockQueryCache = mock(QueryCacheService.class);
        RedisService mockRedis = mock(RedisService.class);
        doThrow(new RuntimeException("fail")).when(mockQueryCache).clearAllCaches();
        RedisStartupCleanerHelper helper = new RedisStartupCleanerHelper(mockQueryCache, mockRedis);
        // Should not throw
        helper.cleanRedisOnStartup();
        verify(mockQueryCache).clearAllCaches();
        // deletePattern có thể không được gọi nếu exception xảy ra trước
    }
}