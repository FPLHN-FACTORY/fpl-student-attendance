package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDUpdateLinkMeetRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDUserStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStudentRepository;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SPDPlanDateServiceImpl implements SPDPlanDateService {

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDPlanFactoryRepository spdPlanFactoryRepository;

    private final SPDFacilityShiftRepository spdFacilityShiftRepository;

    private final SPDUserStudentRepository spdUserStudentRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    private final SettingHelper settingHelper;

    private final MailerHelper mailerHelper;

    private final UserActivityLogHelper userActivityLogHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Value("${app.config.allows-one-teacher-to-teach-multiple-classes}")
    private boolean isDisableCheckExistsTeacherOnShift;

    @Override
    public ResponseEntity<?> getDetail(String idPlanFactory) {
        Optional<SPDPlanFactoryResponse> data = spdPlanFactoryRepository.getDetail(idPlanFactory,
                sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateResponse> data = PageableObject
                .of(spdPlanDateRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePlanDate(String idPlanDate) {
        Optional<SPDPlanDateResponse> entity = spdPlanDateRepository.getPlanDateById(idPlanDate,
                sessionHelper.getFacilityId());
        if (entity.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }
        if (spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(),
                List.of(entity.get().getId())) > 0) {

            // Enhanced logging with detailed information
            String logMessage = String.format(
                    "vừa xóa kế hoạch chi tiết ngày %s - Nhóm xưởng: %s - Kế hoạch: %s - Ca : %s",
                    DateTimeUtils.convertMillisToDate(entity.get().getStartDate()),
                    getFactoryNameFromPlanDate(entity.get().getId()),
                    getPlanNameFromPlanDate(entity.get().getId()),
                    entity.get().getShift());
            userActivityLogHelper.saveLog(logMessage);

            return RouterHelper.responseSuccess("Xoá thành công kế hoạch chi tiết.");
        }

        return RouterHelper.responseError("Không thể xoá kế hoạch chi tiết này");
    }

    @Override
    public ResponseEntity<?> deleteMultiplePlanDate(SPDDeletePlanDateRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 mục muốn xoá.");
        }

        int result = spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(), request.getIds());
        if (result > 0) {
            // Enhanced logging with detailed information
            String factoryNames = getFactoryNamesFromPlanDates(request.getIds());
            String planNames = getPlanNamesFromPlanDates(request.getIds());
            String timeRange = getTimeRangeFromPlanDates(request.getIds());
            String specificSessions = getSpecificSessionsFromPlanDates(request.getIds());

            String logMessage = String.format(
                    "vừa xóa %d kế hoạch chi tiết - " +
                            "Nhóm xưởng: %s - " +
                            "Kế hoạch: %s - " +
                            "Thời gian: %s - " +
                            "Các buổi học: %s",
                    result,
                    factoryNames,
                    planNames,
                    timeRange,
                    specificSessions);
            userActivityLogHelper.saveLog(logMessage);

            return RouterHelper.responseSuccess("Xoá thành công " + result + " kế hoạch chi tiết.");
        }
        return RouterHelper.responseError("Không có kế hoạch nào cần xoá");
    }

    @Override
    public ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        int MAX_LATE_ARRIVAL = settingHelper.getSetting(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, Integer.class);

        if (request.getLateArrival() > MAX_LATE_ARRIVAL) {
            return RouterHelper.responseError("Thời gian điểm danh muộn nhất không quá " + MAX_LATE_ARRIVAL + " phút");
        }

        PlanDate planDate = spdPlanDateRepository.findById(request.getId()).orElse(null);

        if (planDate == null
                || !Objects.equals(
                        planDate.getPlanFactory().getFactory().getProject().getSubjectFacility().getFacility().getId(),
                        request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }

        if (DateTimeUtils.getCurrentTimeMillis() > planDate.getEndDate()) {
            return RouterHelper.responseError("Không thể cập nhật kế hoạch đã diễn ra");
        }

        // Store original values for logging
        Long originalStartDate = planDate.getStartDate();
        Long originalEndDate = planDate.getEndDate();
        String originalShift = String.valueOf(planDate.getShift());
        String originalDescription = planDate.getDescription();

        List<List<Integer>> lstShift = ShiftHelper.findConsecutiveShift(request.getShift());

        if (lstShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca ");
        }

        int startShift = lstShift.get(0).get(0);
        int endShift = lstShift.get(0).get(lstShift.get(0).size() - 1);

        FacilityShift shiftStart = spdFacilityShiftRepository.getOneById(startShift, sessionHelper.getFacilityId())
                .orElse(null);
        FacilityShift shiftEnd = spdFacilityShiftRepository.getOneById(endShift, sessionHelper.getFacilityId())
                .orElse(null);
        if (shiftStart == null) {
            return RouterHelper.responseError("Ca  " + startShift + " không tồn tại");
        }
        if (shiftEnd == null) {
            return RouterHelper.responseError("Ca  " + endShift + " không tồn tại");
        }

        ShiftType type;
        try {
            type = ShiftType.fromKey(request.getType());
        } catch (Exception e) {
            return RouterHelper.responseError("Hình thức không hợp lệ");
        }

        Long startDate;
        Long endDate;
        if (request.getCustomTime() != null && request.getCustomTime().size() == 2) {
            startDate = request.getCustomTime().get(0);
            endDate = request.getCustomTime().get(1);
        } else {
            startDate = ShiftHelper.getShiftTimeStart(request.getStartDate(),
                    LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
            endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(),
                    shiftEnd.getToHour(), shiftEnd.getToMinute());
        }

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < planDate.getPlanFactory().getPlan().getFromDate()
                || startDate > planDate.getPlanFactory().getPlan().getToDate()) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ "
                    + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getFromDate()) + " đến "
                    + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getToDate()));
        }

        Factory factory = planDate.getPlanFactory().getFactory();

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getPlanFactory().getId(), planDate.getId(), startDate,
                endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca  diễn ra trong khoảng thời gian từ "
                    + DateTimeUtils.convertMillisToDate(startDate, "HH:mm") + " đến "
                    + DateTimeUtils.convertMillisToDate(endDate, "HH:mm") + " của ngày "
                    + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (!isDisableCheckExistsTeacherOnShift) {
            if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate,
                    planDate.getId())) {
                return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - "
                        + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift()
                        + " trong ngày "
                        + DateTimeUtils.convertMillisToDate(startDate));
            }
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link online không hợp lệ");
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        StatusType requiredCheckin = StatusType.fromKey(request.getRequiredCheckin());
        StatusType requiredCheckout = StatusType.fromKey(request.getRequiredCheckout());
        if (requiredIp == null || requiredLocation == null || requiredCheckin == null || requiredCheckout == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        List<SPDUserStudentResponse> lstStudentExists = spdPlanDateRepository
                .getListExistsStudentOnShift(planDate.getPlanFactory().getFactory().getId(), startDate, endDate, null);
        if (!lstStudentExists.isEmpty()) {
            return RouterHelper.responseError(
                    "Không thể tạo ca học do có sinh viên đang thuộc nhóm xưởng khác có cùng thời gian học",
                    lstStudentExists);
        }

        String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

        planDate.setStartDate(startDate);
        planDate.setEndDate(endDate);
        planDate.setShift(request.getShift());
        planDate.setType(type);
        planDate.setLink(link);
        planDate.setRoom(type == ShiftType.ONLINE ? null : request.getRoom());
        planDate.setRequiredIp(requiredIp);
        planDate.setRequiredLocation(requiredLocation);
        planDate.setRequiredCheckin(requiredCheckin);
        planDate.setRequiredCheckout(requiredCheckout);
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        PlanDate newEntity = spdPlanDateRepository.save(planDate);

        commonUserStudentRepository.disableAllStudentDuplicateShiftByStartDate(
                planDate.getPlanFactory().getFactory().getId(), planDate.getStartDate());

        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format(
                "vừa cập nhật lịch ngày %s - Nhóm xưởng: %s - Kế hoạch: %s",
                DateTimeUtils.convertMillisToDate(newEntity.getStartDate()),
                factory.getName(),
                planDate.getPlanFactory().getPlan().getName()));

        if (!Objects.equals(originalStartDate, startDate) || !Objects.equals(originalEndDate, endDate)) {
            logMessage.append(String.format(
                    " - Thay đổi thời gian: từ %s-%s thành %s-%s",
                    DateTimeUtils.convertMillisToDate(originalStartDate, "HH:mm"),
                    DateTimeUtils.convertMillisToDate(originalEndDate, "HH:mm"),
                    DateTimeUtils.convertMillisToDate(startDate, "HH:mm"),
                    DateTimeUtils.convertMillisToDate(endDate, "HH:mm")));
        }

        if (!Objects.equals(originalShift, request.getShift())) {
            logMessage.append(String.format(
                    " - Thay đổi ca: từ %s thành %s",
                    originalShift,
                    request.getShift()));
        }

        if (!Objects.equals(originalDescription, request.getDescription())) {
            logMessage.append(String.format(
                    " - Thay đổi mô tả: từ '%s' thành '%s'",
                    originalDescription != null ? originalDescription : "",
                    request.getDescription() != null ? request.getDescription() : ""));
        }

        userActivityLogHelper.saveLog(logMessage.toString());
        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> addPlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        int MAX_LATE_ARRIVAL = settingHelper.getSetting(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, Integer.class);

        if (request.getLateArrival() > MAX_LATE_ARRIVAL) {
            return RouterHelper.responseError("Thời gian điểm danh muộn nhất không quá " + MAX_LATE_ARRIVAL + " phút");
        }

        PlanFactory planFactory = spdPlanFactoryRepository.findById(request.getIdPlanFactory()).orElse(null);

        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch nhóm xưởng");
        }

        Plan plan = planFactory.getPlan();

        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(),
                        request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = planFactory.getFactory();

        if (factory == null
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(),
                        request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        List<List<Integer>> lstShift = ShiftHelper.findConsecutiveShift(request.getShift());

        if (lstShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca ");
        }

        int startShift = lstShift.get(0).get(0);
        int endShift = lstShift.get(0).get(lstShift.get(0).size() - 1);

        FacilityShift shiftStart = spdFacilityShiftRepository.getOneById(startShift, sessionHelper.getFacilityId())
                .orElse(null);
        FacilityShift shiftEnd = spdFacilityShiftRepository.getOneById(endShift, sessionHelper.getFacilityId())
                .orElse(null);
        if (shiftStart == null) {
            return RouterHelper.responseError("Ca học " + startShift + " không tồn tại");
        }
        if (shiftEnd == null) {
            return RouterHelper.responseError("Ca học " + endShift + " không tồn tại");
        }

        ShiftType type;
        try {
            type = ShiftType.fromKey(request.getType());
        } catch (Exception e) {
            return RouterHelper.responseError("Hình thức học không hợp lệ");
        }

        Long startDate;
        Long endDate;
        if (request.getCustomTime() != null && request.getCustomTime().size() == 2) {
            startDate = request.getCustomTime().get(0);
            endDate = request.getCustomTime().get(1);
        } else {
            startDate = ShiftHelper.getShiftTimeStart(request.getStartDate(),
                    LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
            endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(),
                    shiftEnd.getToHour(), shiftEnd.getToMinute());
        }

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < plan.getFromDate() || startDate > plan.getToDate()) {
            return RouterHelper.responseError(
                    "Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(plan.getFromDate())
                            + " đến " + DateTimeUtils.convertMillisToDate(plan.getToDate()));
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca học diễn ra trong khoảng thời gian từ "
                    + DateTimeUtils.convertMillisToDate(startDate, "HH:mm") + " đến "
                    + DateTimeUtils.convertMillisToDate(endDate, "HH:mm") + " của ngày "
                    + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (!isDisableCheckExistsTeacherOnShift) {
            if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate,
                    null)) {
                return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - "
                        + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift()
                        + " trong ngày "
                        + DateTimeUtils.convertMillisToDate(startDate));
            }
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link học online không hợp lệ");
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        StatusType requiredCheckin = StatusType.fromKey(request.getRequiredCheckin());
        StatusType requiredCheckout = StatusType.fromKey(request.getRequiredCheckout());
        if (requiredIp == null || requiredLocation == null || requiredCheckin == null || requiredCheckout == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        List<SPDUserStudentResponse> lstStudentExists = spdPlanDateRepository
                .getListExistsStudentOnShift(planFactory.getFactory().getId(), startDate, endDate, null);
        if (!lstStudentExists.isEmpty()) {
            return RouterHelper.responseError(
                    "Không thể tạo ca học do có sinh viên đang thuộc nhóm xưởng khác có cùng thời gian học",
                    lstStudentExists);
        }

        String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

        PlanDate planDate = new PlanDate();
        planDate.setPlanFactory(planFactory);
        planDate.setStartDate(startDate);
        planDate.setEndDate(endDate);
        planDate.setShift(request.getShift());
        planDate.setType(type);
        planDate.setLink(link);
        planDate.setRoom(type == ShiftType.ONLINE ? null : request.getRoom());
        planDate.setRequiredIp(requiredIp);
        planDate.setRequiredLocation(requiredLocation);
        planDate.setRequiredCheckin(requiredCheckin);
        planDate.setRequiredCheckout(requiredCheckout);
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        PlanDate newEntity = spdPlanDateRepository.save(planDate);

        commonUserStudentRepository.disableAllStudentDuplicateShiftByStartDate(
                planDate.getPlanFactory().getFactory().getId(), planDate.getStartDate());

        // Enhanced logging with detailed information
        String logMessage = String.format(
                "vừa thêm kế hoạch chi tiết ngày %s - Nhóm xưởng: %s - Kế hoạch: %s - Ca học: %s - Thời gian: %s-%s",
                DateTimeUtils.convertMillisToDate(newEntity.getStartDate()),
                factory.getName(),
                plan.getName(),
                request.getShift(),
                DateTimeUtils.convertMillisToDate(startDate, "HH:mm"),
                DateTimeUtils.convertMillisToDate(endDate, "HH:mm"));
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Thêm kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> updateLinkMeet(SPDUpdateLinkMeetRequest request) {
        if (!ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link học online không hợp lệ");
        }
        PlanFactory planFactory = spdPlanFactoryRepository.findById(request.getIdPlanFactory()).orElse(null);
        if (planFactory == null || !planFactory.getPlan().getProject().getSubjectFacility().getFacility().getId()
                .equals(sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy nhóm kế hoạch");
        }
        spdPlanDateRepository.updateAllLinkMeet(planFactory.getId(), request.getLink());

        // Enhanced logging with detailed information
        String logMessage = String.format(
                "vừa cập nhật link học online cho nhóm xưởng: %s - Kế hoạch: %s - Link: %s",
                planFactory.getFactory().getName(),
                planFactory.getPlan().getName(),
                request.getLink());
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Cập nhật link học online thành công");
    }

    @Override
    public ResponseEntity<?> sendMail(String idPlanFactory) {
        Optional<SPDPlanFactoryResponse> planFactoryResponseOptional = spdPlanFactoryRepository.getDetail(idPlanFactory,
                sessionHelper.getFacilityId());
        if (planFactoryResponseOptional.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        PlanFactory planFactory = spdPlanFactoryRepository.findById(idPlanFactory).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        List<SPDPlanDateResponse> lstPlanDate = spdPlanDateRepository.getAllListNotRunning(idPlanFactory);
        if (lstPlanDate.isEmpty()) {
            return RouterHelper.responseError("Không có ca học nào sắp diễn ra");
        }

        byte[] file = createFileMail(lstPlanDate);
        if (file == null) {
            return RouterHelper.responseError("Không thể tạo tệp tin lịch học");
        }

        MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
        mailerDefaultRequest.setTemplate(null);
        mailerDefaultRequest.setTo(planFactory.getFactory().getUserStaff().getEmail());
        mailerDefaultRequest
                .setTitle("Thông báo lịch học sắp tới - " + planFactory.getFactory().getName() + " - " + appName);

        List<String> lstEmails = spdUserStudentRepository.getAllEmail(planFactory.getFactory().getId());

        if (!lstEmails.isEmpty()) {
            mailerDefaultRequest.setBcc(lstEmails.toArray(new String[0]));
        }

        Map<String, Object> dataMail = new HashMap<>();
        dataMail.put("STAFF_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());

        Map<String, byte[]> filesMail = new HashMap<>();
        filesMail.put("lich-hoc-sap-toi.xlsx", file);

        mailerDefaultRequest.setAttachments(filesMail);
        mailerDefaultRequest
                .setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_UPCOMING_SCHEDULE_PLAN_DATE, dataMail));
        mailerHelper.send(mailerDefaultRequest);

        // Enhanced logging with detailed information
        String logMessage = String.format(
                "vừa gửi mail thông báo lịch  sắp tới cho nhóm xưởng: %s - Kế hoạch: %s - Số ca : %d",
                planFactory.getFactory().getName(),
                planFactory.getPlan().getName(),
                lstPlanDate.size());
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Gửi mail thông báo ca  sắp diễn ra thành công");
    }

    private byte[] createFileMail(List<SPDPlanDateResponse> lstPlanDate) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream data = new ByteArrayOutputStream()) {

            List<String> headers = new ArrayList<>(
                    List.of("Ngày ", "Thời gian", "Ca ", "Phòng ", "Link online"));
            Sheet sheet = ExcelUtils.createTemplate(workbook, "Lịch ", headers, new ArrayList<>());
            int row = 1;
            for (SPDPlanDateResponse planDateResponse : lstPlanDate) {
                List<Object> dataCell = new ArrayList<>();
                dataCell.add(DateTimeUtils.convertMillisToDate(planDateResponse.getStartDate()));
                dataCell.add(DateTimeUtils.convertMillisToDate(planDateResponse.getStartDate(), "HH:mm") + " - "
                        + DateTimeUtils.convertMillisToDate(planDateResponse.getEndDate(), "HH:mm"));

                String shiftFormatted = Arrays.stream(planDateResponse.getShift().split(","))
                        .map(String::trim)
                        .map(Integer::valueOf)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

                dataCell.add("Ca " + shiftFormatted + " - " + ShiftType.fromKey(planDateResponse.getType()));
                dataCell.add(planDateResponse.getRoom());
                dataCell.add(planDateResponse.getLink());
                ExcelUtils.insertRow(sheet, row, dataCell);
                row++;
            }

            workbook.write(data);
            return data.toByteArray();
        } catch (IOException e) {
            return null;
        }

    }

    // Helper methods for enhanced logging
    private String getFactoryNameFromPlanDate(String planDateId) {
        try {
            Optional<SPDPlanDateResponse> planDate = spdPlanDateRepository.getPlanDateById(planDateId,
                    sessionHelper.getFacilityId());
            if (planDate.isPresent()) {
                // Get factory name from PlanFactory relationship
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null && planDateEntity.getPlanFactory() != null
                        && planDateEntity.getPlanFactory().getFactory() != null) {
                    return planDateEntity.getPlanFactory().getFactory().getName();
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
        return "Nhóm xưởng";
    }

    private String getPlanNameFromPlanDate(String planDateId) {
        try {
            Optional<SPDPlanDateResponse> planDate = spdPlanDateRepository.getPlanDateById(planDateId,
                    sessionHelper.getFacilityId());
            if (planDate.isPresent()) {
                // Get plan name from PlanFactory relationship
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null && planDateEntity.getPlanFactory() != null
                        && planDateEntity.getPlanFactory().getPlan() != null) {
                    return planDateEntity.getPlanFactory().getPlan().getName();
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
        return "Kế hoạch";
    }

    private String getFactoryNamesFromPlanDates(List<String> planDateIds) {
        try {
            if (planDateIds == null || planDateIds.isEmpty()) {
                return "Nhiều nhóm xưởng";
            }

            // Get unique factory names from multiple plan dates
            Set<String> factoryNames = new HashSet<>();
            for (String planDateId : planDateIds) {
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null && planDateEntity.getPlanFactory() != null
                        && planDateEntity.getPlanFactory().getFactory() != null) {
                    factoryNames.add(planDateEntity.getPlanFactory().getFactory().getName());
                }
            }

            if (factoryNames.isEmpty()) {
                return "Nhiều nhóm xưởng";
            } else if (factoryNames.size() == 1) {
                return factoryNames.iterator().next();
            } else {
                return String.join(", ", factoryNames);
            }
        } catch (Exception e) {
            return "Nhiều nhóm xưởng";
        }
    }

    private String getPlanNamesFromPlanDates(List<String> planDateIds) {
        try {
            if (planDateIds == null || planDateIds.isEmpty()) {
                return "Nhiều kế hoạch";
            }

            // Get unique plan names from multiple plan dates
            Set<String> planNames = new HashSet<>();
            for (String planDateId : planDateIds) {
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null && planDateEntity.getPlanFactory() != null
                        && planDateEntity.getPlanFactory().getPlan() != null) {
                    planNames.add(planDateEntity.getPlanFactory().getPlan().getName());
                }
            }

            if (planNames.isEmpty()) {
                return "Nhiều kế hoạch";
            } else if (planNames.size() == 1) {
                return planNames.iterator().next();
            } else {
                return String.join(", ", planNames);
            }
        } catch (Exception e) {
            return "Nhiều kế hoạch";
        }
    }

    private String getTimeRangeFromPlanDates(List<String> planDateIds) {
        try {
            if (planDateIds == null || planDateIds.isEmpty()) {
                return "Không có thông tin thời gian";
            }

            Long earliestStart = null;
            Long latestEnd = null;

            for (String planDateId : planDateIds) {
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null) {
                    if (earliestStart == null || planDateEntity.getStartDate() < earliestStart) {
                        earliestStart = planDateEntity.getStartDate();
                    }
                    if (latestEnd == null || planDateEntity.getEndDate() > latestEnd) {
                        latestEnd = planDateEntity.getEndDate();
                    }
                }
            }

            if (earliestStart != null && latestEnd != null) {
                String startTime = DateTimeUtils.convertMillisToDate(earliestStart, "dd/MM/yyyy HH:mm");
                String endTime = DateTimeUtils.convertMillisToDate(latestEnd, "dd/MM/yyyy HH:mm");
                return startTime + " đến " + endTime;
            }
        } catch (Exception e) {
        }
        return "Không xác định thời gian";
    }

    private String getSpecificSessionsFromPlanDates(List<String> planDateIds) {
        try {
            if (planDateIds == null || planDateIds.isEmpty()) {
                return "Không có thông tin buổi học";
            }

            List<String> sessionDetails = new ArrayList<>();
            for (String planDateId : planDateIds) {
                PlanDate planDateEntity = spdPlanDateRepository.findById(planDateId).orElse(null);
                if (planDateEntity != null) {
                    String date = DateTimeUtils.convertMillisToDate(planDateEntity.getStartDate(), "dd/MM/yyyy");
                    String time = DateTimeUtils.convertMillisToDate(planDateEntity.getStartDate(), "HH:mm") +
                            " - " + DateTimeUtils.convertMillisToDate(planDateEntity.getEndDate(), "HH:mm");
                    String shift = "Ca " + planDateEntity.getShift();
                    String type = planDateEntity.getType().name();
                    String room = planDateEntity.getRoom() != null ? planDateEntity.getRoom() : "Không có phòng";

                    String sessionInfo = String.format("%s %s (%s - %s - %s)",
                            date, time, shift, type, room);
                    sessionDetails.add(sessionInfo);
                }
            }

            if (sessionDetails.isEmpty()) {
                return "Không có thông tin buổi học";
            } else if (sessionDetails.size() <= 3) {
                return String.join(", ", sessionDetails);
            } else {
                return String.format("%s, ... và %d buổi khác",
                        String.join(", ", sessionDetails.subList(0, 3)),
                        sessionDetails.size() - 3);
            }
        } catch (Exception e) {
        }
        return "Không xác định buổi học";
    }

}
