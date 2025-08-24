package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingKeysTest {
    @Test
    void testEnumValues() {
        for (SettingKeys key : SettingKeys.values()) {
            assertNotNull(key);
        }
    }

    @Test
    void testGetKeyAndFromKey() {
        assertEquals(0, SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF.getKey());
        assertEquals(1, SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT.getKey());
        assertEquals(2, SettingKeys.SHIFT_MIN_DIFF.getKey());
        assertEquals(3, SettingKeys.SHIFT_MAX_LATE_ARRIVAL.getKey());
        assertEquals(4, SettingKeys.ATTENDANCE_EARLY_CHECKIN.getKey());
        assertEquals(5, SettingKeys.EXPIRATION_MINUTE_LOGIN.getKey());
        assertEquals(6, SettingKeys.FACE_THRESHOLD_CHECKIN.getKey());
        assertEquals(7, SettingKeys.FACE_THRESHOLD_REGISTER.getKey());

        assertEquals(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, SettingKeys.fromKey(0));
        assertEquals(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, SettingKeys.fromKey(1));
        assertEquals(SettingKeys.SHIFT_MIN_DIFF, SettingKeys.fromKey(2));
        assertEquals(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, SettingKeys.fromKey(3));
        assertEquals(SettingKeys.ATTENDANCE_EARLY_CHECKIN, SettingKeys.fromKey(4));
        assertEquals(SettingKeys.EXPIRATION_MINUTE_LOGIN, SettingKeys.fromKey(5));
        assertEquals(SettingKeys.FACE_THRESHOLD_CHECKIN, SettingKeys.fromKey(6));
        assertEquals(SettingKeys.FACE_THRESHOLD_REGISTER, SettingKeys.fromKey(7));
        assertNull(SettingKeys.fromKey(100));
    }

    @Test
    void testToString() {
        assertEquals("DISABLED_CHECK_EMAIL_FPT_STAFF", SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF.toString());
        assertEquals("DISABLED_CHECK_EMAIL_FPT_STUDENT", SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT.toString());
    }
}
