package udpm.hn.studentattendance.infrastructure.constants;

import lombok.Getter;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Getter
public enum ShiftConstant {

    CA1(LocalTime.of(7, 15), LocalTime.of(9, 15)),
    CA2(LocalTime.of(9, 25), LocalTime.of(11, 25)),
    CA3(LocalTime.of(12, 0), LocalTime.of(14, 0)),
    CA4(LocalTime.of(14, 10), LocalTime.of(16, 10)),
    CA5(LocalTime.of(16, 20), LocalTime.of(18, 20)),
    CA6(LocalTime.of(18, 30), LocalTime.of(20, 30));

    private final LocalTime startTime;

    private final LocalTime endTime;

    ShiftConstant(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ShiftConstant getCurrentShift() {
        LocalTime now = LocalTime.now();
        for (ShiftConstant shift : ShiftConstant.values()) {
            if ((now.isAfter(shift.getStartTime()) ||
                    now.equals(shift.getStartTime())) && now.isBefore(shift.getEndTime())
            ) {
                return shift;
            }
        }
        return null;
    }

    public static Long getShiftTimeStart(long time, ShiftConstant shift) {
        LocalDate date = DateTimeUtils.convertTimestampToLocalDate(time);
        LocalDateTime startDateTime = date.atTime(shift.getStartTime());
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        return startDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeStart(LocalDate date, ShiftConstant shift) {
        LocalDateTime startDateTime = date.atTime(shift.getStartTime());
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        return startDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeEnd(long time, ShiftConstant shift) {
        LocalDate date = DateTimeUtils.convertTimestampToLocalDate(time);
        LocalDateTime endDateTime = date.atTime(shift.getEndTime());
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        return endDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeEnd(LocalDate date, ShiftConstant shift) {
        LocalDateTime endDateTime = date.atTime(shift.getEndTime());
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        return endDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

}
