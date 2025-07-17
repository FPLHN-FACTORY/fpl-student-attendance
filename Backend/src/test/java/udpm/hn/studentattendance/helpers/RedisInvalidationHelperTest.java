package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisInvalidationHelperTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private RedisInvalidationHelper redisInvalidationHelper;

    @BeforeEach
    void setUp() {
        // Setup default behavior
        doNothing().when(redisService).deletePattern(anyString());
    }

    @Test
    void testInvalidateAllCaches() {
        // Act
        redisInvalidationHelper.invalidateAllCaches();

        // Assert - verify that all patterns are deleted
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_STUDENT + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_STAFF + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_TEACHER + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACTORY + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_PLAN + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_SUBJECT + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_LEVEL + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_IP + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_LOCATION + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACILITY_SHIFT + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_SUBJECT_FACILITY + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_ADMIN + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "*");
        verify(redisService).deletePattern(RedisPrefixConstant.REDIS_PREFIX_STATISTICS + "*");
        verify(redisService).deletePattern("redis_*");
    }
}