package udpm.hn.studentattendance.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonScheduleReminderRepository;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleTeacherAndStudentRemindHelper {

    private final CommonScheduleReminderRepository commonScheduleReminderRepository;
    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    private static final String ZONE_ID = "Asia/Ho_Chi_Minh";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(cron = "0 0 20 * * SUN")
    public void sendWeeklyScheduleReminders() {
        log.info("Bắt đầu gửi email nhắc nhở lịch hàng tuần");

        try {
            LocalDate nextMonday = getNextMonday();
            LocalDate nextSunday = nextMonday.plusDays(6);

            long startTimeRange = nextMonday.atStartOfDay(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();
            long endTimeRange = nextSunday.atTime(23, 59, 59).atZone(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();

            log.info("Gửi nhắc nhở lịch từ {} đến {}",
                    nextMonday.format(DATE_FORMATTER),
                    nextSunday.format(DATE_FORMATTER));

            List<PlanDate> upcomingPlanDates = commonScheduleReminderRepository.findUpcomingPlanDates(
                    startTimeRange,
                    endTimeRange,
                    EntityStatus.ACTIVE,
                    EntityStatus.ACTIVE);

            if (upcomingPlanDates.isEmpty()) {
                log.info("Không có lịch nào trong tuần tới");
                return;
            }

            Map<String, List<PlanDate>> planDatesByFactory = upcomingPlanDates.stream()
                    .collect(Collectors.groupingBy(pd -> pd.getPlanFactory().getFactory().getId()));

            for (Map.Entry<String, List<PlanDate>> entry : planDatesByFactory.entrySet()) {
                String factoryId = entry.getKey();
                List<PlanDate> planDates = entry.getValue();

                sendFactoryScheduleReminders(factoryId, planDates, nextMonday, nextSunday);
            }

            log.info("Hoàn thành gửi email nhắc nhở lịch hàng tuần");

        } catch (Exception e) {
            log.error("Lỗi khi gửi email nhắc nhở lịch hàng tuần: {}", e.getMessage(), e);
        }
    }

    private void sendFactoryScheduleReminders(String factoryId, List<PlanDate> planDates,
            LocalDate weekStart, LocalDate weekEnd) {
        try {
            List<UserStudentFactory> students = commonScheduleReminderRepository
                    .findStudentsByFactoryIdAndStatus(factoryId, EntityStatus.ACTIVE);

            List<String> teacherEmails = commonScheduleReminderRepository
                    .findTeachersByFactoryId(factoryId);

            if (students.isEmpty() && teacherEmails.isEmpty()) {
                log.warn("Không tìm thấy sinh viên hoặc giảng viên cho factory: {}", factoryId);
                return;
            }

            String emailContent = createWeeklyScheduleEmailContent(planDates, weekStart, weekEnd)
                    .replace("{{ APP_NAME }}", appName);

            if (!students.isEmpty()) {
                sendStudentScheduleReminders(students, emailContent, weekStart, weekEnd);
            }

            if (!teacherEmails.isEmpty()) {
                sendTeacherScheduleReminders(teacherEmails, emailContent, weekStart, weekEnd);
            }

            log.info("Đã gửi email nhắc nhở lịch cho factory: {}", factoryId);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email nhắc nhở lịch cho factory {}: {}", factoryId, e.getMessage(), e);
        }
    }

    private void sendStudentScheduleReminders(List<UserStudentFactory> students, String emailContent,
            LocalDate weekStart, LocalDate weekEnd) {
        for (UserStudentFactory studentFactory : students) {
            try {
                String studentEmail = studentFactory.getUserStudent().getEmail();
                String studentName = studentFactory.getUserStudent().getName();
                String factoryName = studentFactory.getFactory().getName();

                MailerDefaultRequest mailRequest = new MailerDefaultRequest();
                mailRequest.setTo(studentEmail);
                mailRequest.setTitle(String.format("Nhắc nhở lịch học tuần %s - %s - %s",
                        weekStart.format(DATE_FORMATTER),
                        weekEnd.format(DATE_FORMATTER),
                        factoryName));
                mailRequest.setContent(emailContent);
                mailRequest.setTemplate(MailerHelper.TEMPLATE_WEEKLY_SCHEDULE_REMINDER);

                mailerHelper.send(mailRequest);

                log.debug("Đã gửi email nhắc nhở lịch cho sinh viên: {} ({})", studentName, studentEmail);

            } catch (Exception e) {
                log.error("Lỗi khi gửi email cho sinh viên {}: {}",
                        studentFactory.getUserStudent().getName(), e.getMessage());
            }
        }
    }

    private void sendTeacherScheduleReminders(List<String> teacherEmails, String emailContent,
            LocalDate weekStart, LocalDate weekEnd) {
        for (String teacherEmail : teacherEmails) {
            try {
                MailerDefaultRequest mailRequest = new MailerDefaultRequest();
                mailRequest.setTo(teacherEmail);
                mailRequest.setTitle(String.format("Nhắc nhở lịch giảng dạy tuần %s - %s",
                        weekStart.format(DATE_FORMATTER),
                        weekEnd.format(DATE_FORMATTER)));
                mailRequest.setContent(emailContent);
                mailRequest.setTemplate(MailerHelper.TEMPLATE_WEEKLY_SCHEDULE_REMINDER);

                mailerHelper.send(mailRequest);

                log.debug("Đã gửi email nhắc nhở lịch cho giảng viên: {}", teacherEmail);

            } catch (Exception e) {
                log.error("Lỗi khi gửi email cho giảng viên {}: {}", teacherEmail, e.getMessage());
            }
        }
    }

    private String createWeeklyScheduleEmailContent(List<PlanDate> planDates, LocalDate weekStart, LocalDate weekEnd) {
        StringBuilder content = new StringBuilder();

        Map<LocalDate, List<PlanDate>> planDatesByDay = planDates.stream()
                .collect(Collectors.groupingBy(pd -> DateTimeUtils.convertTimestampToLocalDate(pd.getStartDate())));

        List<LocalDate> sortedDays = planDatesByDay.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        for (LocalDate day : sortedDays) {
            content.append("<div class='day-schedule'>");
            content.append("<h3 class='day-header'>").append(day.format(DATE_FORMATTER)).append("</h3>");
            content.append("<table class='schedule-table'>");
            content.append("<thead>");
            content.append("<tr>");
            content.append("<th>Thời gian</th>");
            content.append("<th>Ca</th>");
            content.append("<th>Phòng</th>");
            content.append("<th>Link</th>");
            content.append("<th>Mô tả</th>");
            content.append("</tr>");
            content.append("</thead>");
            content.append("<tbody>");

            List<PlanDate> dayPlanDates = planDatesByDay.get(day);
            dayPlanDates.sort(Comparator.comparing(PlanDate::getStartDate));

            for (PlanDate planDate : dayPlanDates) {
                content.append("<tr>");

                String startTime = DateTimeUtils.convertMillisToDate(planDate.getStartDate(), "HH:mm");
                String endTime = DateTimeUtils.convertMillisToDate(planDate.getEndDate(), "HH:mm");
                content.append("<td class='time-cell'>").append(startTime).append(" - ").append(endTime)
                        .append("</td>");

                content.append("<td class='shift-cell'>Ca ").append(planDate.getShift()).append("</td>");

                content.append("<td class='room-cell'>").append(planDate.getRoom() != null ? planDate.getRoom() : "")
                        .append("</td>");

                content.append("<td>").append(planDate.getLink() != null ? planDate.getLink() : "");
                content.append("</td>");

                content.append("<td>").append(planDate.getDescription() != null ? planDate.getDescription() : "")
                        .append("</td>");

                content.append("</tr>");
            }

            content.append("</tbody>");
            content.append("</table>");
            content.append("</div>");
        }

        return content.toString();
    }

    private LocalDate getNextMonday() {
        LocalDate today = LocalDate.now(ZoneId.of(ZONE_ID));
        LocalDate nextMonday = today;

        while (nextMonday.getDayOfWeek().getValue() != 1) { // 1 = Monday
            nextMonday = nextMonday.plusDays(1);
        }

        return nextMonday;
    }

    public void sendManualScheduleReminders(String factoryId, LocalDate startDate, LocalDate endDate) {
        try {
            long startTimeRange = startDate.atStartOfDay(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();
            long endTimeRange = endDate.atTime(23, 59, 59).atZone(ZoneId.of(ZONE_ID)).toInstant().toEpochMilli();

            List<PlanDate> planDates = commonScheduleReminderRepository.findUpcomingPlanDates(
                    startTimeRange,
                    endTimeRange,
                    EntityStatus.ACTIVE,
                    EntityStatus.ACTIVE);

            if (!planDates.isEmpty()) {
                sendFactoryScheduleReminders(factoryId, planDates, startDate, endDate);
            }

        } catch (Exception e) {
            log.error("Lỗi khi gửi email nhắc nhở lịch thủ công: {}", e.getMessage(), e);
        }
    }
}
