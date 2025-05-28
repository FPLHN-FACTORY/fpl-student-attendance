package udpm.hn.studentattendance.infrastructure.constants;

public enum EntityStatus {
    INACTIVE,
    ACTIVE;

    public static EntityStatus fromValue(Integer value) {
        if (value == null) return null;
        return EntityStatus.values()[value];
    }
}
