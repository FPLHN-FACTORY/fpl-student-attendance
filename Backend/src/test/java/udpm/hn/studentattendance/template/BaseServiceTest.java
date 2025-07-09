package udpm.hn.studentattendance.template;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * A base test class that provides common mocks and setup methods for service
 * tests.
 * Extend this class to avoid duplicating mock setups for common dependencies.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    @Mock
    protected RedisService redisService;

    @Mock
    protected RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    protected SessionHelper sessionHelper;

    @Mock
    protected UserActivityLogHelper userActivityLogHelper;

    @Mock
    protected SettingHelper settingHelper;

    /**
     * Sets up common fields using ReflectionTestUtils
     * 
     * @param serviceInstance the service instance to set fields on
     */
    protected void setupCommonFields(Object serviceInstance) {
        // Common configuration fields that most services have
        trySetField(serviceInstance, "redisTTL", 3600L);
        trySetField(serviceInstance, "appName", "Student Attendance App");

        // Optional fields that are present in some services
        trySetField(serviceInstance, "isDisableCheckEmailFpt", "false");
        trySetField(serviceInstance, "apiVersion", "1.0");
        trySetField(serviceInstance, "emailFrom", "noreply@example.com");
        trySetField(serviceInstance, "baseUrl", "http://localhost:8080");
    }

    /**
     * Setups common mock behaviors for helper classes
     */
    protected void setupCommonMocks() {
        // SessionHelper common mocks
        lenient().when(sessionHelper.getUserId()).thenReturn("test-user-id");
        lenient().when(sessionHelper.getUserCode()).thenReturn("TEST-USER");
        lenient().when(sessionHelper.getUserName()).thenReturn("Test User");

        // SettingHelper common mocks
        lenient().when(settingHelper.getSetting(any(SettingKeys.class), any())).thenReturn(false);

        // UserActivityLogHelper common mocks
        lenient().doNothing().when(userActivityLogHelper).saveLog(anyString());

        // RedisInvalidationHelper common mocks
        lenient().doNothing().when(redisInvalidationHelper).invalidateAllCaches();

        // RedisService common mocks for pattern deletion
        lenient().doNothing().when(redisService).deletePattern(anyString());
    }

    /**
     * Create a common entity status value for testing
     * 
     * @param isActive Whether the status should be active
     * @return EntityStatus.ACTIVE or EntityStatus.INACTIVE
     */
    protected EntityStatus getEntityStatus(boolean isActive) {
        return isActive ? EntityStatus.ACTIVE : EntityStatus.INACTIVE;
    }

    /**
     * Tries to set a field using ReflectionTestUtils, ignoring any exceptions if
     * the field doesn't exist
     */
    private void trySetField(Object target, String fieldName, Object value) {
        try {
            ReflectionTestUtils.setField(target, fieldName, value);
        } catch (Exception ignored) {
            // Field doesn't exist in this class or other issue, just ignore
        }
    }
}