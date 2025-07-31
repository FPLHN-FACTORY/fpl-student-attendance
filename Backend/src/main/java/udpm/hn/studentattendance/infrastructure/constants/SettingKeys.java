package udpm.hn.studentattendance.infrastructure.constants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SettingKeys {
    DISABLED_CHECK_EMAIL_FPT_STAFF(0),
    DISABLED_CHECK_EMAIL_FPT_STUDENT(1),
    SHIFT_MIN_DIFF(2),
    SHIFT_MAX_LATE_ARRIVAL(3),
    ATTENDANCE_EARLY_CHECKIN(4),
    EXPIRATION_MINUTE_LOGIN(5);

    private final int key;

    private static final Map<Integer, SettingKeys> ENUM_MAP =
            Stream.of(SettingKeys.values()).collect(Collectors.toMap(SettingKeys::getKey, e -> e));

    SettingKeys(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static SettingKeys fromKey(Integer key) {
        return ENUM_MAP.getOrDefault(key, null);
    }

}
