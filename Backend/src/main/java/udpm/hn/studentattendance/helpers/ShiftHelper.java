package udpm.hn.studentattendance.helpers;

import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ShiftHelper {

    private static final ZoneId ZONEID = ZoneId.of("Asia/Ho_Chi_Minh");

    public static Long getShiftTimeStart(long time, LocalTime startTime) {
        LocalDate date = DateTimeUtils.convertTimestampToLocalDate(time);
        LocalDateTime startDateTime = date.atTime(startTime);
        return startDateTime.atZone(ZONEID).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeStart(LocalDate date, LocalTime startTime) {
        LocalDateTime startDateTime = date.atTime(startTime);
        return startDateTime.atZone(ZONEID).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeEnd(long time, LocalTime endTime) {
        LocalDate date = DateTimeUtils.convertTimestampToLocalDate(time);
        LocalDateTime endDateTime = date.atTime(endTime);
        return endDateTime.atZone(ZONEID).toInstant().toEpochMilli();
    }

    public static Long getShiftTimeEnd(LocalDate date, LocalTime endTime) {
        LocalDateTime endDateTime = date.atTime(endTime);
        return endDateTime.atZone(ZONEID).toInstant().toEpochMilli();
    }

}
