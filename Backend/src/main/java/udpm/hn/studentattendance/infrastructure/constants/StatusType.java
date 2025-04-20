package udpm.hn.studentattendance.infrastructure.constants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StatusType {
    DISABLE(0),
    ENABLE(1);

    private final int key;

    private static final Map<Integer, StatusType> ENUM_MAP =
            Stream.of(StatusType.values()).collect(Collectors.toMap(StatusType::getKey, e -> e));

    StatusType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static StatusType fromKey(int key) {
        return ENUM_MAP.getOrDefault(key, null);
    }

}
