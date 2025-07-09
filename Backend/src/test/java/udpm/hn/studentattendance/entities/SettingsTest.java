package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {
    @Test
    void testNoArgsConstructor() {
        Settings settings = new Settings();
        assertNull(settings.getKey());
        assertNull(settings.getValue());
    }

    @Test
    void testAllArgsConstructor() {
        Settings settings = new Settings(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, "true");
        assertEquals(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, settings.getKey());
        assertEquals("true", settings.getValue());
    }

    @Test
    void testSettersAndGetters() {
        Settings settings = new Settings();
        settings.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        settings.setValue("true");
        assertEquals(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, settings.getKey());
        assertEquals("true", settings.getValue());
    }

    @Test
    void testEqualsAndHashCode() {
        Settings s1 = new Settings();
        s1.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        s1.setValue("true");

        Settings s2 = new Settings();
        s2.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        s2.setValue("true");

        Settings s3 = new Settings();
        s3.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        s3.setValue("false");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1.hashCode(), s3.hashCode());
    }

    @Test
    void testToString() {
        Settings settings = new Settings();
        settings.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        settings.setValue("true");
        String toString = settings.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Settings"));
        assertTrue(toString.contains("key=DISABLED_CHECK_EMAIL_FPT_STAFF"));
        assertTrue(toString.contains("value=true"));
    }

    @Test
    void testEqualsWithNull() {
        Settings settings = new Settings();
        settings.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        assertNotEquals(null, settings);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Settings settings = new Settings();
        settings.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        Object other = new Object();
        assertNotEquals(settings, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Settings settings = new Settings();
        settings.setKey(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF);
        assertEquals(settings, settings);
    }
}