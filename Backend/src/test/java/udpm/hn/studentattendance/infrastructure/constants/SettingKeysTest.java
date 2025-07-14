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
}