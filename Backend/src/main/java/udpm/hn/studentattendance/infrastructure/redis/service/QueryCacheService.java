package udpm.hn.studentattendance.infrastructure.redis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.infrastructure.redis.serializer.CacheWrapper;


@Service
public class QueryCacheService {

    private final RedisService redisService;
    private final boolean enableQueryCache;

    @Value("${spring.cache.redis.time-to-live}")
    private long defaultTtlSeconds;

    public QueryCacheService(RedisService redisService,
            @Value("${app.config.cache.enable-query-cache:true}") boolean enableQueryCache) {
        this.redisService = redisService;
        this.enableQueryCache = enableQueryCache;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCachedResult(String key) {
        if (!enableQueryCache) {
            return null;
        }

        try {
            String fullKey = "query:" + key;
            Object result = redisService.get(fullKey);

            if (result instanceof CacheWrapper) {
                try {
                    result = ((CacheWrapper<?>) result).getData();
                } catch (Exception e) {
                    result = null;
                }
            }

            return (T) result;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> void cacheResult(String key, T value, long ttlSeconds) {
        if (!enableQueryCache) {
            return;
        }

        try {
            String fullKey = "query:" + key;
            CacheWrapper<T> wrapper = CacheWrapper.of(value);
            redisService.set(fullKey, wrapper, ttlSeconds);
        } catch (Exception e) {
        }
    }

    public <T> void cacheResult(String key, T value) {
        cacheResult(key, value, defaultTtlSeconds);
    }

    public void clearCache(String key) {
        try {
            redisService.delete("query:" + key);
        } catch (Exception e) {
        }
    }

    public void clearAllCaches() {
        try {
            redisService.deletePattern("query:*");
        } catch (Exception e) {
        }
    }
}