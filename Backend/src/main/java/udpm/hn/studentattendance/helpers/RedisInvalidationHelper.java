package udpm.hn.studentattendance.helpers;

import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

@Component
public class RedisInvalidationHelper {

    private final RedisService redisService;

    public RedisInvalidationHelper(RedisService redisService) {
        this.redisService = redisService;
    }

    public void invalidateAllCaches() {
        try {
            // Student caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_STUDENT + "*");

            // Staff caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_STAFF + "*");

            // Teacher caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_TEACHER + "*");

            // Factory caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACTORY + "*");
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "*");

            // Plan caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_PLAN + "*");

            // Attendance recovery caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "*");

            // Subject caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SUBJECT + "*");

            // Level caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_LEVEL + "*");

            // Facility caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY + "*");
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_IP + "*");
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_LOCATION + "*");
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_SHIFT + "*");

            // Subject facility caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SUBJECT_FACILITY + "*");

            // Admin caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_ADMIN + "*");

            // Schedule caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "*");
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "*");

            // Statistics caches
            redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_STATISTICS + "*");

            // Catch-all for any keys that might have been missed
            redisService.deletePattern("redis_*");
        } catch (Exception ignored) {
        }
    }
}