package udpm.hn.studentattendance.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.config.redis.service.QueryCacheService;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

@Component
public class RedisStartupCleanerHelper {

    private static final Logger logger = LoggerFactory.getLogger(RedisStartupCleanerHelper.class);

    private final QueryCacheService queryCacheService;

    private final RedisService redisService;

    @Autowired
    public RedisStartupCleanerHelper(
            QueryCacheService queryCacheService,
            RedisService redisService) {
        this.queryCacheService = queryCacheService;
        this.redisService = redisService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void cleanRedisOnStartup() {
        try {
            logger.info("Cleaning Redis cache on application startup");

            queryCacheService.clearAllCaches();

            redisService.deletePattern("*");

            logger.info("Redis cache cleaned successfully");
        } catch (Exception e) {
            logger.warn("Failed to clean Redis cache: {}", e.getMessage());
        }
    }
}