package udpm.hn.studentattendance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    
    private static final String ZONE_ID = "Asia/Ho_Chi_Minh";

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

    public static long convertStringToTimeMillis(String dateString) {
        return convertStringToTimeMillis(dateString, DATE_FORMAT);
    }

    public static long convertStringToTimeMillis(String dateString, String formatDate) {
        boolean hasTime = dateString.contains(":");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatDate);
        LocalDateTime localDateTime;
        if (hasTime) {
            localDateTime = LocalDateTime.parse(dateString, formatter);
        } else {
            localDateTime = LocalDate.parse(dateString, formatter).atStartOfDay();
        }
        ZonedDateTime dateTime = localDateTime.atZone(ZoneId.of(ZONE_ID));
        return dateTime.toInstant().toEpochMilli();
    }

    public static Long getCurrentTime() {
        return new Date().getTime();
    }

    public static Long getCurrentTimeMillis() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZONE_ID));
        return now.toInstant().toEpochMilli();
    }

    public static long toStartOfDay(long timestamp) {
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of(ZONE_ID))
                .toLocalDateTime();
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
        return startOfDay.atZone(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();
    }

    public static long toEndOfDay(long timestamp) {
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of(ZONE_ID))
                .toLocalDateTime();
        LocalDateTime endOfDay = dateTime.toLocalDate()
                .atTime(23, 59, 59, 999_999_999);
        return endOfDay.atZone(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();
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

    public static LocalDate convertTimestampToLocalDate(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return instant.atZone(ZoneId.of(ZONE_ID)).toLocalDate();
    }

}