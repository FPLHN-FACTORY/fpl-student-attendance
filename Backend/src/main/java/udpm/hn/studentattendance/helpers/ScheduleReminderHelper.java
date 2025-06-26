package udpm.hn.studentattendance.helpers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonScheduleReminderRepository;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScheduleReminderHelper {

    private final CommonScheduleReminderRepository scheduleReminderRepository;
    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Value("${app.config.email.schedule-reminder.enabled:true}")
    private boolean scheduleReminderEnabled;

    @Scheduled(cron = "${app.config.email.schedule-reminder.cron}")
    @Transactional
    public void sendClassReminders() {
        if (!scheduleReminderEnabled) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long oneHourLater = currentTime + 60 * 60 * 1000;

        long startTimeRange = currentTime + 50 * 60 * 1000;
        long endTimeRange = currentTime + 70 * 60 * 1000;

        List<PlanDate> upcomingPlanDates = findUpcomingPlanDates(startTimeRange, endTimeRange);

        for (PlanDate planDate : upcomingPlanDates) {
            sendReminderForPlanDate(planDate);
        }
    }

    /**
     * Find plan dates that will start within the specified time range
     *
     * @param startTimeRange Lower bound of start time
     * @param endTimeRange   Upper bound of start time
     * @return List of upcoming plan dates
     */
    private List<PlanDate> findUpcomingPlanDates(long startTimeRange, long endTimeRange) {
        return scheduleReminderRepository.findUpcomingPlanDates(
                startTimeRange,
                endTimeRange,
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE);
    }

    /**
     * Send reminder emails to all students enrolled in this plan date's factory
     *
     * @param planDate The plan date for which to send reminders
     */
    private void sendReminderForPlanDate(PlanDate planDate) {
        String factoryId = planDate.getPlanFactory().getFactory().getId();

        // Get class details for the email
        String factoryName = planDate.getPlanFactory().getFactory().getName();
        String projectName = planDate.getPlanFactory().getFactory().getProject().getName();
        String subjectName = planDate.getPlanFactory().getFactory().getProject().getSubjectFacility().getSubject()
                .getName();
        String staffName = planDate.getPlanFactory().getFactory().getUserStaff().getName();
        String staffCode = planDate.getPlanFactory().getFactory().getUserStaff().getUsername();
        String staffEmail = planDate.getPlanFactory().getFactory().getUserStaff().getEmail();

        String location = planDate.getRoom() != null ? planDate.getRoom() : "Chưa có thông tin";
        String shiftStr = formatShift(planDate);
        String classType = planDate.getType() == null ? "Offline"
                : (planDate.getType() == udpm.hn.studentattendance.infrastructure.constants.ShiftType.ONLINE ? "Online"
                        : "Offline");
        String link = planDate.getLink();

        LocalDateTime startDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(planDate.getStartDate()),
                ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(planDate.getEndDate()),
                ZoneId.systemDefault());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String classDate = startDateTime.format(dateFormatter);
        String startTime = startDateTime.format(timeFormatter);
        String endTime = endDateTime.format(timeFormatter);

        // Send reminder to teacher
        if (staffEmail != null && !staffEmail.trim().isEmpty()) {
            sendTeacherReminderEmail(
                    staffEmail,
                    staffName,
                    factoryName,
                    projectName,
                    subjectName,
                    location,
                    shiftStr,
                    classType,
                    link,
                    classDate,
                    startTime,
                    endTime);
        }

        // Find all active students in this factory
        List<UserStudentFactory> studentFactories = scheduleReminderRepository.findStudentsByFactoryIdAndStatus(
                factoryId, EntityStatus.ACTIVE);

        if (studentFactories.isEmpty()) {
            return;
        }

        // Send email to each student
        for (UserStudentFactory studentFactory : studentFactories) {
            UserStudent student = studentFactory.getUserStudent();

            // Skip if no email
            if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
                continue;
            }

            sendStudentReminderEmail(
                    student.getEmail(),
                    student.getName(),
                    factoryName,
                    projectName,
                    subjectName,
                    staffName,
                    staffCode,
                    location,
                    shiftStr,
                    classType,
                    link,
                    classDate,
                    startTime,
                    endTime);
        }
    }

    /**
     * Format the shift values into a readable string
     *
     * @param planDate The plan date containing shift information
     * @return Formatted shift string
     */
    private String formatShift(PlanDate planDate) {
        if (planDate.getShift() == null || planDate.getShift().isEmpty()) {
            return "Không xác định";
        }

        return ShiftHelper.getShiftsString(planDate.getShift());
    }

    /**
     * Send the actual reminder email to a student
     */
    private void sendStudentReminderEmail(
            String studentEmail,
            String studentName,
            String factoryName,
            String projectName,
            String subjectName,
            String staffName,
            String staffCode,
            String location,
            String shiftStr,
            String classType,
            String link,
            String classDate,
            String startTime,
            String endTime) {

        Map<String, Object> data = new HashMap<>();
        data.put("STUDENT_NAME", studentName);
        data.put("FACTORY_NAME", factoryName);
        data.put("PROJECT_NAME", projectName);
        data.put("SUBJECT_NAME", subjectName);
        data.put("STAFF_NAME", staffName);
        data.put("STAFF_CODE", staffCode);
        data.put("LOCATION", location);
        data.put("SHIFT", shiftStr);
        data.put("CLASS_TYPE", classType);
        data.put("CLASS_DATE", classDate);
        data.put("START_TIME", startTime);
        data.put("END_TIME", endTime);
        data.put("LINK", link != null && !link.trim().isEmpty() ? link : "Không có");

        String content = MailerHelper.loadTemplate("class-reminder.html", data);

        MailerDefaultRequest mailRequest = new MailerDefaultRequest();
        mailRequest.setTo(studentEmail);
        mailRequest.setTitle("[" + appName + "] Nhắc nhở: Sắp có lịch học vào lúc " + startTime);
        mailRequest.setContent(content);

        // Send email asynchronously
        mailerHelper.send(mailRequest);
    }

    /**
     * Send reminder email to a teacher about upcoming class
     */
    private void sendTeacherReminderEmail(
            String teacherEmail,
            String teacherName,
            String factoryName,
            String projectName,
            String subjectName,
            String location,
            String shiftStr,
            String classType,
            String link,
            String classDate,
            String startTime,
            String endTime) {

        Map<String, Object> data = new HashMap<>();
        data.put("TEACHER_NAME", teacherName);
        data.put("FACTORY_NAME", factoryName);
        data.put("PROJECT_NAME", projectName);
        data.put("SUBJECT_NAME", subjectName);
        data.put("LOCATION", location);
        data.put("SHIFT", shiftStr);
        data.put("CLASS_TYPE", classType);
        data.put("CLASS_DATE", classDate);
        data.put("START_TIME", startTime);
        data.put("END_TIME", endTime);
        data.put("LINK", link != null && !link.trim().isEmpty() ? link : "Không có");

        String content = MailerHelper.loadTemplate("teacher-class-reminder.html", data);

        MailerDefaultRequest mailRequest = new MailerDefaultRequest();
        mailRequest.setTo(teacherEmail);
        mailRequest.setTitle("[" + appName + "] Nhắc nhở: Sắp có lịch dạy vào lúc " + startTime);
        mailRequest.setContent(content);

        // Send email asynchronously
        mailerHelper.send(mailRequest);
    }
}