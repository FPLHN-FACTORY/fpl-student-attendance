package udpm.hn.studentattendance.helpers;

import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ShiftHelper {

    public final static int MIN_DIFF = 1800; // 30 phút

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

    public static int getDiffTime(int fromHour, int fromMinute, int toHour, int toMinute) {
        FacilityShift shift = new FacilityShift();
        shift.setFromHour(fromHour);
        shift.setToHour(toHour);
        shift.setFromMinute(fromMinute);
        shift.setToMinute(toMinute);
        return getDiffTime(shift);
    }

    public static int getDiffTime(FacilityShift shift) {
        int fromInSeconds = shift.getFromHour() * 3600 + shift.getFromMinute() * 60;
        int toInSeconds = shift.getToHour() * 3600 + shift.getToMinute() * 60;
        return (toInSeconds - fromInSeconds) * 1000;
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
