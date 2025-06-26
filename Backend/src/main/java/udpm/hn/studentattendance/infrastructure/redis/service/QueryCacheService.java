package udpm.hn.studentattendance.infrastructure.redis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage data query caching
 */
@Service
public class QueryCacheService {

    private static final Logger logger = LoggerFactory.getLogger(QueryCacheService.class);

    private final RedisService redisService;
    private final boolean enableQueryCache;
    private final ThreadLocal<Boolean> cacheHitThreadLocal = new ThreadLocal<>();
    private final Map<String, CacheStats> cacheStats = new ConcurrentHashMap<>();

    @Value("${spring.cache.redis.time-to-live:3600}")
    private long defaultTtlSeconds;

    public QueryCacheService(RedisService redisService,
            @Value("${app.config.cache.enable-query-cache:true}") boolean enableQueryCache) {
        this.redisService = redisService;
        this.enableQueryCache = enableQueryCache;
        logger.info("Query cache is {}", enableQueryCache ? "enabled" : "disabled");
    }

    /**
     * Get cached query result
     * 
     * @param key Cache key
     * @return Cached value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedResult(String key) {
        // Reset cache hit flag
        cacheHitThreadLocal.set(false);
        
        if (!enableQueryCache) {
            logger.debug("Query cache is disabled, skipping cache lookup for key {}", key);
            return null;
        }
        
        try {
            String fullKey = "query:" + key;
            Object result = redisService.get(fullKey);
            if (result != null) {
                // Set cache hit flag to true if we got a result from cache
                cacheHitThreadLocal.set(true);
                updateStats(key, true);
                logger.info("Cache HIT for key {}", key);
            } else {
                updateStats(key, false);
                logger.info("Cache MISS for key {}", key);
            }
            return (T) result;
        } catch (Exception e) {
            logger.warn("Error getting cached result for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Check if the last cache operation was a hit
     * 
     * @return true if the last operation was a cache hit
     */
    public boolean wasCacheHit() {
        Boolean wasHit = cacheHitThreadLocal.get();
        return wasHit != null && wasHit;
    }

    /**
     * Cache query result
     * 
     * @param key        Cache key
     * @param value      Value to cache
     * @param ttlSeconds TTL in seconds
     */
    public <T> void cacheResult(String key, T value, long ttlSeconds) {
        if (!enableQueryCache) {
            logger.debug("Query cache is disabled, skipping cache storage for key {}", key);
            return;
        }
        
        try {
            String fullKey = "query:" + key;
            redisService.set(fullKey, value, ttlSeconds);
            logger.info("Cached result for key {} with TTL {}s", key, ttlSeconds);
        } catch (Exception e) {
            logger.warn("Error caching result for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Cache query result with default TTL
     * 
     * @param key   Cache key
     * @param value Value to cache
     */
    public <T> void cacheResult(String key, T value) {
        cacheResult(key, value, defaultTtlSeconds);
    }

    /**
     * Clear cache for a specific key
     * 
     * @param key Cache key
     */
    public void clearCache(String key) {
        try {
            redisService.delete("query:" + key);
            logger.debug("Cleared cache for key {}", key);
        } catch (Exception e) {
            logger.warn("Error clearing cache for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Clear all query caches
     */
    public void clearAllCaches() {
        try {
            redisService.deletePattern("query:*");
            cacheStats.clear();
            logger.info("Cleared all query caches");
        } catch (Exception e) {
            logger.warn("Error clearing all query caches: {}", e.getMessage());
        }
    }

    /**
     * Check if query cache is enabled
     * 
     * @return true if query cache is enabled, false otherwise
     */
    public boolean isQueryCacheEnabled() {
        return enableQueryCache;
    }

    /**
     * Update cache statistics
     */
    private void updateStats(String key, boolean hit) {
        cacheStats.computeIfAbsent(key, k -> new CacheStats()).update(hit);
    }

    /**
     * Get cache statistics
     */
    public Map<String, CacheStats> getCacheStats() {
        return new HashMap<>(cacheStats);
    }

    /**
     * Cache statistics class
     */
    public static class CacheStats {
        private int hits;
        private int misses;

        public void update(boolean hit) {
            if (hit) {
                hits++;
            } else {
                misses++;
            }
        }

        public int getHits() {
            return hits;
        }

        public int getMisses() {
            return misses;
        }

        public double getHitRatio() {
            int total = hits + misses;
            return total > 0 ? (double) hits / total : 0;
        }
    }
}