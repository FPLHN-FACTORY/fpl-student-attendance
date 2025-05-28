package udpm.hn.studentattendance.infrastructure.constants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum IPType {
    IPV4(0),
    IPV6(1),
    DNSSUFFIX(2);

    private final int key;

    private static final Map<Integer, IPType> ENUM_MAP =
            Stream.of(IPType.values()).collect(Collectors.toMap(IPType::getKey, e -> e));

    IPType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static IPType fromKey(Integer key) {
        return ENUM_MAP.getOrDefault(key, null);
    }
}
