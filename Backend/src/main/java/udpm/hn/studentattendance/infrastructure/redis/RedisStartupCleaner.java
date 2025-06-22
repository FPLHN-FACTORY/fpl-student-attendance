package udpm.hn.studentattendance.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.redis.service.QueryCacheService;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.infrastructure.redis.service.SessionCacheService;

/**
 * Component to clean Redis cache on application startup
 * Prevents issues with old data format after application updates
 */
@Component
public class RedisStartupCleaner {

    private static final Logger logger = LoggerFactory.getLogger(RedisStartupCleaner.class);

    private final SessionCacheService sessionCacheService;
    private final QueryCacheService queryCacheService;
    private final RedisService redisService;

    @Autowired
    public RedisStartupCleaner(SessionCacheService sessionCacheService,
            QueryCacheService queryCacheService,
            RedisService redisService) {
        this.sessionCacheService = sessionCacheService;
        this.queryCacheService = queryCacheService;
        this.redisService = redisService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void cleanRedisOnStartup() {
        try {
            logger.info("Cleaning Redis cache on application startup");

            // Clean session data
            sessionCacheService.clearAllSessions();

            // Clean query cache data
            queryCacheService.clearAllCaches();

            // Clean any other unhandled data
            redisService.flushAll();

            logger.info("Redis cache cleaned successfully");
        } catch (Exception e) {
            logger.warn("Failed to clean Redis cache: {}", e.getMessage());
        }
    }
}