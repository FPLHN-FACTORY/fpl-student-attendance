package udpm.hn.studentattendance.helpers;

import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static List<List<Integer>> findConsecutiveShift(List<Integer> selected) {
        List<List<Integer>> result = new ArrayList<>();
        if (selected == null || selected.isEmpty()) {
            return result;
        }

        Collections.sort(selected);
        List<Integer> currentShift = new ArrayList<>();
        currentShift.add(selected.get(0));

        for (int i = 1; i < selected.size(); i++) {
            int current = selected.get(i);
            int previous = selected.get(i - 1);
            if (current == previous + 1) {
                currentShift.add(current);
            } else {
                result.add(new ArrayList<>(currentShift));
                currentShift.clear();
                currentShift.add(current);
            }
        }

        result.add(currentShift);
        return result;
    }
}
