package udpm.hn.studentattendance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeUtils {

    public static String DATE_FORMAT = "dd/MM/yyyy";

    public static Long parseStringToLong(String date) {
        return parseStringToLong(date, DATE_FORMAT);
    }

    public static Long parseStringToLong(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date dateConvert = dateFormat.parse(date);
            return dateConvert.getTime();
        } catch (ParseException e) {
            try {
                return Long.parseLong(date);
            } catch (NumberFormatException ex) {
                ex.printStackTrace(System.err);
                return null;
            }
        }
    }

    public static String parseLongToString(Long date) {
        return parseLongToString(date, DATE_FORMAT);
    }

    public static String parseLongToString(Long date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date(date));
    }

    public static String convertDateToString(Date date) {
        return convertDateToString(date, DATE_FORMAT);
    }

    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String convertMillisToDate(long millis) {
        return convertMillisToDate(millis, DATE_FORMAT);
    }

    public static String convertMillisToDate(long millis, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(millis));
    }

    public static Long getCurrentTime() {
        return new Date().getTime();
    }

    public static Long getCurrentTimeMillis() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime startOfDay = currentDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        return instant.toEpochMilli();
    }

    public static Long getCurrentDateWithoutTime() {
        LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        LocalDateTime startOfDay = currentDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        return instant.toEpochMilli();
    }

    public static LocalDate convertToLocalDate(Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }


}