package udpm.hn.studentattendance.core.teacher.teachingschedule.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSSubjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.service.TCTeachingScheduleService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Validated
public class TCTeachingScheduleServiceImpl implements TCTeachingScheduleService {

        private final TCTeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;

        private final TCTSProjectExtendRepository teacherTsProjectExtendRepository;

        private final TCTSSubjectExtendRepository teacherTsSubjectExtendRepository;

        private final TCTSFactoryExtendRepository teacherTsFactoryExtendRepository;

        private final UserStudentFactoryRepository userStudentFactoryRepository;

        private final MailerHelper mailerHelper;

        private final SessionHelper sessionHelper;

        private final SettingHelper settingHelper;

        private final RedisService redisService;

        @Value("${app.config.app-name}")
        private String appName;

        private int MAX_LATE_ARRIVAL;

        @Value("${spring.cache.redis.time-to-live}")
        private long redisTTL;

        @PostConstruct
        public void init() {
                this.MAX_LATE_ARRIVAL = settingHelper.getSetting(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, Integer.class);
        }

        public PageableObject<?> getCachedTeachingSchedule(TCTeachingScheduleRequest request) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_"
                                + sessionHelper.getUserId() + "_"
                                + request.toString();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, PageableObject.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                Pageable pageable = PaginationHelper.createPageable(request);
                PageableObject<?> list = PageableObject
                                .of(teacherTeachingScheduleExtendRepository.getAllTeachingScheduleByStaff(
                                                sessionHelper.getUserId(), pageable, request));

                try {
                        redisService.set(cacheKey, list, redisTTL);
                } catch (Exception ignored) {
                }

                return list;
        }

        @Override
        public ResponseEntity<?> getAllTeachingScheduleByStaff(
                        TCTeachingScheduleRequest teachingScheduleRequest) {
                PageableObject<?> list = getCachedTeachingSchedule(teachingScheduleRequest);
                return RouterHelper.responseSuccess(
                                "Lấy tất cả lịch dạy của " + sessionHelper.getUserId() + " thành công", list);
        }

        public List<Factory> getCachedFactories() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "factories_"
                                + sessionHelper.getUserId();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, List.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                List<Factory> factories = teacherTsFactoryExtendRepository
                                .getAllFactoryByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);

                try {
                        redisService.set(cacheKey, factories, redisTTL);
                } catch (Exception ignored) {
                }

                return factories;
        }

        @Override
        public ResponseEntity<?> getAllFactoryByStaff() {
                List<Factory> factories = getCachedFactories();
                return RouterHelper.responseSuccess(
                                "Lấy tất cả nhóm xửng của " + sessionHelper.getUserId() + " dạy thành công", factories);
        }

        public List<Project> getCachedProjects() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "projects_"
                                + sessionHelper.getUserId();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, List.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                List<Project> projects = teacherTsProjectExtendRepository.getAllProject(sessionHelper.getUserId(),
                                EntityStatus.ACTIVE);

                try {
                        redisService.set(cacheKey, projects, redisTTL);
                } catch (Exception ignored) {
                }

                return projects;
        }

        @Override
        public ResponseEntity<?> getAllProjectByStaff() {
                List<Project> projects = getCachedProjects();
                return RouterHelper.responseSuccess(
                                "Lấy tất cả dự án đang dạy của " + sessionHelper.getUserId() + " thành công", projects);
        }

        public List<Subject> getCachedSubjects() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "subjects_"
                                + sessionHelper.getUserId();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, List.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                List<Subject> subjects = teacherTsSubjectExtendRepository
                                .getAllSubjectByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);

                try {
                        redisService.set(cacheKey, subjects, redisTTL);
                } catch (Exception ignored) {
                }

                return subjects;
        }

        @Override
        public ResponseEntity<?> getAllSubjectByStaff() {
                List<Subject> subjects = getCachedSubjects();
                return RouterHelper.responseSuccess(
                                "Lấy tất cả môn học của " + sessionHelper.getUserId() + " thành công", subjects);
        }

        public List<PlanDate> getCachedTypes() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "types";

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, List.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                List<PlanDate> shifts = teacherTeachingScheduleExtendRepository.getAllType();

                try {
                        redisService.set(cacheKey, shifts, redisTTL * 24); // Cache for a day since this doesn't change
                                                                           // often
                } catch (Exception ignored) {
                }

                return shifts;
        }

        @Override
        public ResponseEntity<?> getAllType() {
                List<PlanDate> shifts = getCachedTypes();
                return RouterHelper.responseSuccess("Lấy tất cả hình thức học thành công", shifts);
        }

        @Override
        public ByteArrayInputStream exportTeachingSchedule(
                        List<TCTeachingScheduleResponse> teachingScheduleResponseList) {
                Document document = new Document();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                try {
                        PdfWriter.getInstance(document, byteArrayOutputStream);
                        document.open();

                        BaseFont unicodeFont = BaseFont.createFont("font/Arial Unicode.ttf", BaseFont.IDENTITY_H,
                                        BaseFont.EMBEDDED);
                        Font fontHeaders = new Font(unicodeFont, 15, Font.BOLD);
                        Font headFont = new Font(unicodeFont, 12, Font.SYMBOL, new Color(239, 235, 235));
                        Font cellFont = new Font(unicodeFont, 12);

                        Paragraph paragraph = new Paragraph(
                                        "Lịch dạy giảng viên: "
                                                        + sessionHelper.getUserCode()
                                                        + " - "
                                                        + sessionHelper.getUserName(),
                                        fontHeaders);
                        paragraph.setAlignment(Element.ALIGN_CENTER);
                        document.add(paragraph);
                        document.add(Chunk.NEWLINE);

                        PdfPTable pdfTable = new PdfPTable(8);
                        pdfTable.setWidthPercentage(100);
                        pdfTable.setSpacingBefore(10f);
                        pdfTable.setSpacingAfter(10f);
                        pdfTable.setWidths(new float[] { 50, 30, 30, 20, 30, 30, 25, 30 });

                        Color headerColor = new Color(2, 3, 51);

                        Color rowColor1 = new Color(255, 255, 255);
                        Color rowColor2 = new Color(245, 245, 245);

                        Stream.of("Ngày dạy", "Ca học", "Điểm danh muộn", "Mã môn", "Xưởng", "Địa điểm", "Hình thức",
                                        "Mô tả")
                                        .forEach(headerTitle -> {
                                                PdfPCell headerCell = new PdfPCell();
                                                headerCell.setBackgroundColor(headerColor);
                                                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                                headerCell.setBorderWidth(1);
                                                headerCell.setPadding(8);
                                                headerCell.setPhrase(new Phrase(headerTitle, headFont));
                                                pdfTable.addCell(headerCell);
                                        });

                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE - dd/MM/yyyy HH:mm",
                                        new Locale("vi", "VN"));

                        int rowIndex = 0;
                        for (TCTeachingScheduleResponse teachingScheduleResponse : teachingScheduleResponseList) {
                                Color backgroundColor = (rowIndex % 2 == 0) ? rowColor1 : rowColor2;

                                String formattedTeachingDay = dateFormat
                                                .format(new Date(teachingScheduleResponse.getStartTeaching()));
                                PdfPCell teachingDayCell = new PdfPCell(new Phrase(formattedTeachingDay, cellFont));
                                styleCell(teachingDayCell, backgroundColor);
                                pdfTable.addCell(teachingDayCell);

                                PdfPCell shiftCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getShift()), cellFont));
                                styleCell(shiftCell, backgroundColor);
                                pdfTable.addCell(shiftCell);

                                PdfPCell lateArrivalCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getLateArrival() + " phút"),
                                                cellFont));
                                styleCell(lateArrivalCell, backgroundColor);
                                pdfTable.addCell(lateArrivalCell);

                                PdfPCell subjectCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getSubjectCode()), cellFont));
                                styleCell(subjectCell, backgroundColor);
                                pdfTable.addCell(subjectCell);

                                PdfPCell factoryCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getFactoryName()), cellFont));
                                styleCell(factoryCell, backgroundColor);
                                pdfTable.addCell(factoryCell);

                                PdfPCell roomCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getRoom()), cellFont));
                                styleCell(roomCell, backgroundColor);
                                pdfTable.addCell(roomCell);

                                PdfPCell typeCell = new PdfPCell(new Phrase(
                                                String.valueOf(teachingScheduleResponse.getType() == 0 ? "Offline"
                                                                : "Online"),
                                                cellFont));
                                styleCell(typeCell, backgroundColor);
                                pdfTable.addCell(typeCell);

                                PdfPCell descriptionCell = new PdfPCell(new Phrase(
                                                teachingScheduleResponse.getDescription() != null
                                                                ? teachingScheduleResponse.getDescription()
                                                                : "",
                                                cellFont));
                                styleCell(descriptionCell, backgroundColor);
                                pdfTable.addCell(descriptionCell);

                                rowIndex++;
                        }

                        document.add(pdfTable);
                        document.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }

        private void styleCell(PdfPCell cell, Color backgroundColor) {
                cell.setBackgroundColor(backgroundColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(1);
                cell.setPadding(8);
                cell.setBorderColor(new Color(200, 200, 200));
        }

        public TCTSDetailPlanDateResponse getCachedPlanDateDetail(String planDateId) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "plan_date_" + planDateId;

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, TCTSDetailPlanDateResponse.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                Optional<TCTSDetailPlanDateResponse> getDetailPlanDateResponse = teacherTeachingScheduleExtendRepository
                                .getPlanDateById(planDateId);

                TCTSDetailPlanDateResponse result = getDetailPlanDateResponse.orElse(null);
                if (result != null) {
                        try {
                                redisService.set(cacheKey, result, redisTTL);
                        } catch (Exception ignored) {
                        }
                }

                return result;
        }

        @Override
        public ResponseEntity<?> getDetailPlanDate(String planDateId) {
                TCTSDetailPlanDateResponse detail = getCachedPlanDateDetail(planDateId);
                if (detail != null) {
                        return RouterHelper.responseSuccess("Lấy chi tiết kế hoạch thành công", detail);
                }
                return RouterHelper.responseSuccess("Lấy chi tiết lịch dạy thành công", null);
        }

        @Override
        public ResponseEntity<?> updatePlanDate(TCTSPlanDateUpdateRequest planDateUpdateRequest) {
                Optional<PlanDate> existPlanDate = teacherTeachingScheduleExtendRepository
                                .findById(planDateUpdateRequest.getIdPlanDate());

                if (existPlanDate.isEmpty()) {
                        return RouterHelper.responseError("Không tìm thấy lịch dạy");
                }

                PlanDate planDate = existPlanDate.get();
                if (!Objects.equals(planDate.getPlanFactory().getFactory().getUserStaff().getId(),
                                sessionHelper.getUserId())) {
                        return RouterHelper.responseError("Lịch dạy không phải của bạn");
                }

                boolean isOutOfTime = teacherTeachingScheduleExtendRepository.isOutOfTime(existPlanDate.get().getId());
                if (isOutOfTime) {
                        return RouterHelper.responseError("Đã quá giờ cập nhật ca dạy");
                }

                if (planDateUpdateRequest.getLateArrival() > MAX_LATE_ARRIVAL) {
                        return RouterHelper.responseError(
                                        "Thời gian điểm danh muộn nhất không quá " + MAX_LATE_ARRIVAL + " phút");
                }

                if (StringUtils.hasText(planDateUpdateRequest.getLink())
                                && !ValidateHelper.isValidURL(planDateUpdateRequest.getLink())) {
                        return RouterHelper.responseError("Link học online không hợp lệ");
                }

                String oldDescription = planDate.getDescription();
                Integer oldLateArrival = planDate.getLateArrival();
                String oldLink = planDate.getLink();
                String oldRoom = planDate.getRoom();

                planDate.setDescription(planDateUpdateRequest.getDescription());
                planDate.setLateArrival(planDateUpdateRequest.getLateArrival());
                planDate.setLink(planDateUpdateRequest.getLink());
                planDate.setRoom(planDateUpdateRequest.getRoom());
                PlanDate savedPlanDate = teacherTeachingScheduleExtendRepository.save(planDate);

                boolean hasChanges = !Objects.equals(oldDescription, planDateUpdateRequest.getDescription()) ||
                                !Objects.equals(oldLateArrival, planDateUpdateRequest.getLateArrival()) ||
                                !Objects.equals(oldLink, planDateUpdateRequest.getLink()) ||
                                !Objects.equals(oldRoom, planDateUpdateRequest.getRoom());

                if (hasChanges) {
                        sendUpdateNotificationToStudents(savedPlanDate,
                                        "Thông báo cập nhật lịch học",
                                        "Thông tin lịch học đã được cập nhật");
                }

                // Invalidate related caches
                invalidatePlanDateCache(planDateUpdateRequest.getIdPlanDate());

                return RouterHelper.responseSuccess("Cập nhật thông tin buổi học thành công", savedPlanDate);
        }

        private void sendUpdateNotificationToStudents(PlanDate planDate, String subject, String notificationType) {
                try {
                        String factoryId = planDate.getPlanFactory().getFactory().getId();

                        Factory factory = planDate.getPlanFactory().getFactory();
                        String factoryName = factory.getName();
                        String projectName = factory.getProject().getName();
                        String subjectName = factory.getProject().getSubjectFacility().getSubject().getName();
                        String staffName = factory.getUserStaff().getName() + " (" + factory.getUserStaff().getCode()
                                        + ")";

                        String shiftStr = ShiftHelper.getShiftsString(planDate.getShift());
                        String classType = planDate.getType() == ShiftType.ONLINE ? "Online" : "Offline";
                        String location = planDate.getRoom() != null ? planDate.getRoom() : "Chưa có thông tin";
                        String link = planDate.getLink() != null && !planDate.getLink().trim().isEmpty()
                                        ? planDate.getLink()
                                        : "Không có";

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

                        List<UserStudentFactory> studentFactories = userStudentFactoryRepository.findAll().stream()
                                        .filter(usf -> usf.getStatus() == EntityStatus.ACTIVE)
                                        .filter(usf -> usf.getFactory().getStatus() == EntityStatus.ACTIVE)
                                        .filter(usf -> usf.getFactory().getId().equals(factoryId))
                                        .filter(usf -> usf.getUserStudent().getStatus() == EntityStatus.ACTIVE)
                                        .toList();

                        for (UserStudentFactory studentFactory : studentFactories) {
                                UserStudent student = studentFactory.getUserStudent();
                                if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
                                        continue;
                                }

                                Map<String, Object> data = new HashMap<>();
                                data.put("STUDENT_NAME", student.getName());
                                data.put("NOTIFICATION_TYPE", notificationType);
                                data.put("FACTORY_NAME", factoryName);
                                data.put("PROJECT_NAME", projectName);
                                data.put("SUBJECT_NAME", subjectName);
                                data.put("STAFF_NAME", staffName);
                                data.put("LOCATION", location);
                                data.put("SHIFT", shiftStr);
                                data.put("CLASS_TYPE", classType);
                                data.put("CLASS_DATE", classDate);
                                data.put("START_TIME", startTime);
                                data.put("END_TIME", endTime);
                                data.put("LINK", link);
                                data.put("LATE_ARRIVAL", planDate.getLateArrival() + " phút");
                                data.put("DESCRIPTION", planDate.getDescription() != null ? planDate.getDescription()
                                                : "Không có");

                                String content = MailerHelper.loadTemplate("schedule-update-notification.html", data);

                                MailerDefaultRequest mailRequest = new MailerDefaultRequest();
                                mailRequest.setTo(student.getEmail());
                                mailRequest.setTitle("[" + appName + "] " + subject);
                                mailRequest.setContent(content);

                                mailerHelper.send(mailRequest);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public PageableObject<?> getCachedCurrentTeachingSchedule(TCTeachingScheduleRequest request) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "current_"
                                + sessionHelper.getUserId() + "_"
                                + request.toString();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, PageableObject.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                Pageable pageable = PaginationHelper.createPageable(request);
                PageableObject<?> list = PageableObject
                                .of(teacherTeachingScheduleExtendRepository.getAllTeachingSchedulePresent(
                                                sessionHelper.getUserId(), pageable, request));

                try {
                        redisService.set(cacheKey, list, redisTTL / 2); // Shorter TTL for current schedules as they
                                                                        // change more often
                } catch (Exception ignored) {
                }

                return list;
        }

        @Override
        public ResponseEntity<?> getAllTeachingSchedulePresent(
                        TCTeachingScheduleRequest teachingScheduleRequest) {
                PageableObject<?> list = getCachedCurrentTeachingSchedule(teachingScheduleRequest);
                return RouterHelper.responseSuccess("Lấy tất cả lịch dạy hiện tại thành công", list);
        }

        @Override
        public ResponseEntity<?> changeTypePlanDate(String planDateId, String room) {
                Optional<PlanDate> existPlanDate = teacherTeachingScheduleExtendRepository.findById(planDateId);
                if (existPlanDate.isEmpty()) {
                        return RouterHelper.responseError("Không tìm thấy lịch dạy");
                }

                PlanDate planDate = existPlanDate.get();
                if (!Objects.equals(planDate.getPlanFactory().getFactory().getUserStaff().getId(),
                                sessionHelper.getUserId())) {
                        return RouterHelper.responseError("Lịch dạy không phải của bạn");
                }

                boolean isOutOfTime = teacherTeachingScheduleExtendRepository.isOutOfTime(existPlanDate.get().getId());
                if (isOutOfTime) {
                        return RouterHelper.responseError("Đã quá giờ cập nhật ca dạy");
                }

                ShiftType oldType = planDate.getType();

                planDate.setType(planDate.getType() == ShiftType.ONLINE ? ShiftType.OFFLINE : ShiftType.ONLINE);
                planDate.setRequiredIp(planDate.getRequiredIp() == StatusType.DISABLE ? StatusType.ENABLE
                                : StatusType.DISABLE);
                planDate.setRequiredLocation(planDate.getRequiredLocation() == StatusType.DISABLE ? StatusType.ENABLE
                                : StatusType.DISABLE);
                planDate.setRoom(planDate.getType() == ShiftType.ONLINE ? "" : room);
                planDate.setLink(planDate.getType() == ShiftType.ONLINE ? planDate.getLink() : "");
                PlanDate savedPlanDate = teacherTeachingScheduleExtendRepository.save(planDate);

                String notificationType = "Thay đổi hình thức học từ " +
                                (oldType == ShiftType.ONLINE ? "Online" : "Offline") +
                                " sang " +
                                (savedPlanDate.getType() == ShiftType.ONLINE ? "Online" : "Offline");

                sendUpdateNotificationToStudents(savedPlanDate,
                                "Thông báo thay đổi hình thức học",
                                notificationType);

                // Invalidate related caches
                invalidatePlanDateCache(planDateId);

                return RouterHelper.responseSuccess("Thay đổi hình thức học thành công", savedPlanDate);
        }

        /**
         * Xóa cache liên quan đến một kế hoạch cụ thể
         */
        private void invalidatePlanDateCache(String planDateId) {
                // Xóa cache chi tiết kế hoạch
                redisService.delete(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "plan_date_" + planDateId);

                // Xóa cache danh sách kế hoạch (cả hiện tại và tất cả)
                invalidateTeachingScheduleCaches();

                // Xóa cache lịch học sinh viên vì việc thay đổi này ảnh hưởng đến lịch học của
                // sinh viên
                redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_*");
        }

        /**
         * Xóa tất cả cache lịch dạy
         */
        private void invalidateTeachingScheduleCaches() {
                String userId = sessionHelper.getUserId();
                redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_*");
                redisService.deletePattern(
                                RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "current_" + userId + "_*");
        }
}