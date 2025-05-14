package udpm.hn.studentattendance.infrastructure.constants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ShiftType {
    OFFLINE(0),
    ONLINE(1);

    private final int key;

    private static final Map<Integer, ShiftType> ENUM_MAP =
            Stream.of(ShiftType.values()).collect(Collectors.toMap(ShiftType::getKey, e -> e));

    ShiftType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static ShiftType fromKey(Integer key) {
        return ENUM_MAP.getOrDefault(key, null);
    }
}
