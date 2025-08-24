package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.core.staff.plan.model.dto.SPDPlanDateGroupDto;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDSearchTeacherRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDUpdateLinkMeetRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateGroupResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDTeacherResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDUserStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStaffRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStudentRepository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
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
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
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

    private final SPDUserStaffRepository spdUserStaffRepository;

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
        Page<SPDPlanDateGroupResponse> lstDataGroup = spdPlanDateRepository.getAllGroupByFilter(pageable, request);

        List<SPDPlanDateGroupDto> lstData = new ArrayList<>();
        List<SPDPlanDateGroupResponse> content = lstDataGroup.getContent();

        for (SPDPlanDateGroupResponse item : content) {
            SPDPlanDateGroupDto o = new SPDPlanDateGroupDto();
            o.setOrderNumber(item.getOrderNumber());
            o.setStartDate(item.getStartDate());
            o.setDay(item.getDay());
            o.setTotalShift(item.getTotalShift());

            List<SPDPlanDateResponse> items = spdPlanDateRepository.getAllByFilter(item.getDay(), request);
            Set<Integer> types = new HashSet<>();
            for(SPDPlanDateResponse p: items) {
                types.add(p.getType());
            }
            o.setTypes(types);
            o.setStatus(item.getStatus());
            o.setPlanDates(items);
            lstData.add(o);
        }

        PageableObject<SPDPlanDateGroupDto> data = new PageableObject<>();
        data.setData(lstData);
        data.setTotalPages(lstDataGroup.getTotalPages());
        data.setTotalItems(lstDataGroup.getTotalElements());
        data.setCurrentPage(lstDataGroup.getNumber());

        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePlanDate(String idPlanDate) {
        Optional<SPDPlanDateResponse> entity = spdPlanDateRepository.getPlanDateById(idPlanDate,
                sessionHelper.getFacilityId());

        PlanDate planDate = spdPlanDateRepository.findById(idPlanDate).orElse(null);
        if (entity.isEmpty() || planDate == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }

        if (spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(),
                List.of(entity.get().getId())) > 0) {

            // Enhanced logging with detailed information
            String logMessage = String.format(
                    "vừa xóa 1 ca ngày %s - Nhóm xưởng: %s - Kế hoạch: %s - Ca : %s",
                    DateTimeUtils.convertMillisToDate(entity.get().getStartDate()),
                    planDate.getPlanFactory().getFactory().getName(),
                    planDate.getPlanFactory().getPlan().getName(),
                    entity.get().getShift());
            userActivityLogHelper.saveLog(logMessage);

            return RouterHelper.responseSuccess("Xoá ca thành công.");
        }

        return RouterHelper.responseError("Không thể xoá ca này");
    }

    @Override
    public ResponseEntity<?> deleteMultiplePlanDate(SPDDeletePlanDateRequest request) {
        if (request.getDays() == null || request.getDays().isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 mục muốn xoá.");
        }

        PlanFactory planFactory = spdPlanFactoryRepository.findById(request.getIdPlanFactory()).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch nhóm xưởng.");
        }

        int result = spdPlanDateRepository.deletePlanDateByDay(sessionHelper.getFacilityId(), request.getDays());
        if (result > 0) {
            // Enhanced logging with detailed information
            String logMessage = String.format(
                    "vừa xóa %d ca ngày: %s - " +
                            "Nhóm xưởng: %s - " +
                            "Kế hoạch: %s",
                    result,
                    String.join(", ", request.getDays()),
                    planFactory.getFactory().getName(),
                    planFactory.getPlan().getName());
            userActivityLogHelper.saveLog(logMessage);

            return RouterHelper.responseSuccess("Xoá thành công " + result + " ca.");
        }
        return RouterHelper.responseError("Không có ca nào cần xoá");
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
            return RouterHelper.responseError("Không tìm thấy ca");
        }

        if (DateTimeUtils.getCurrentTimeMillis() > planDate.getEndDate()) {
            return RouterHelper.responseError("Không thể cập nhật ca đã diễn ra");
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

        if (StringUtils.hasText(request.getRoom()) && type == ShiftType.OFFLINE) {
            if (!ValidateHelper.isValidName(request.getRoom())) {
                return RouterHelper.responseError("Tên phòng chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
            }
            if (spdPlanDateRepository.isExistsRoomOnShift(request.getRoom(), startDate, endDate, planDate.getId())) {
                return RouterHelper.responseError("Địa điểm " + request.getRoom() + " đã được sử dụng vào ca " + request.getShift()
                        + " trong ngày "
                        + DateTimeUtils.convertMillisToDate(startDate));
            }
        }


        Factory factory = planDate.getPlanFactory().getFactory();

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getPlanFactory().getId(), planDate.getId(), startDate,
                endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca diễn ra trong khoảng thời gian từ "
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

        if (type == ShiftType.ONLINE && !StringUtils.hasText(request.getLink())) {
            return RouterHelper.responseError("Link online không được bỏ trống");
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link online không hợp lệ");
        }

        UserStaff teacher = null;
        if (StringUtils.hasText(request.getIdTeacher())) {
            teacher = spdUserStaffRepository.findById(request.getIdTeacher()).orElse(null);
            if (teacher == null || teacher.getStatus() == EntityStatus.INACTIVE) {
                return RouterHelper.responseError("Không tìm thấy giảng viên dạy thay");
            }

            if (!isDisableCheckExistsTeacherOnShift) {
                if (spdPlanDateRepository.isExistsTeacherOnShift(teacher.getId(), startDate, endDate,
                        planDate.getId())) {
                    return RouterHelper.responseError("Giảng viên " + teacher.getName() + " - "
                            + teacher.getCode() + " đã đứng lớp tại ca " + request.getShift()
                            + " trong ngày "
                            + DateTimeUtils.convertMillisToDate(startDate));
                }
            }
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        StatusType requiredCheckin = StatusType.fromKey(request.getRequiredCheckin());
        StatusType requiredCheckout = StatusType.fromKey(request.getRequiredCheckout());
        if (requiredIp == null || requiredLocation == null || requiredCheckin == null || requiredCheckout == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        List<SPDUserStudentResponse> lstStudentExists = spdPlanDateRepository
                .getListExistsStudentOnShift(planDate.getPlanFactory().getFactory().getId(), startDate, endDate, planDate.getId());
        if (!lstStudentExists.isEmpty()) {
            return RouterHelper.responseError(
                    "Không thể cập nhật ca do có sinh viên đang thuộc nhóm xưởng khác có cùng thời gian",
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
        planDate.setUserStaff(teacher);

        PlanDate newEntity = spdPlanDateRepository.save(planDate);

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
        return RouterHelper.responseSuccess("Cập nhật ca thành công", newEntity);
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
            return RouterHelper.responseError("Ca " + startShift + " không tồn tại");
        }
        if (shiftEnd == null) {
            return RouterHelper.responseError("Ca " + endShift + " không tồn tại");
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

        if (startDate < plan.getFromDate() || startDate > plan.getToDate()) {
            return RouterHelper.responseError(
                    "Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(plan.getFromDate())
                            + " đến " + DateTimeUtils.convertMillisToDate(plan.getToDate()));
        }

        if (StringUtils.hasText(request.getRoom()) && type == ShiftType.OFFLINE) {
            if (!ValidateHelper.isValidName(request.getRoom())) {
                return RouterHelper.responseError("Tên phòng chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
            }
            if (spdPlanDateRepository.isExistsRoomOnShift(request.getRoom(), startDate, endDate,
                    null)) {
                return RouterHelper.responseError("Địa điểm " + request.getRoom() + " đã được sử dụng vào ca " + request.getShift()
                        + " trong ngày "
                        + DateTimeUtils.convertMillisToDate(startDate));
            }
        }

        if (type == ShiftType.ONLINE && !StringUtils.hasText(request.getLink())) {
            return RouterHelper.responseError("Link online không được bỏ trống");
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link online không hợp lệ");
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca diễn ra trong khoảng thời gian từ "
                    + DateTimeUtils.convertMillisToDate(startDate, "HH:mm") + " đến "
                    + DateTimeUtils.convertMillisToDate(endDate, "HH:mm") + " của ngày "
                    + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (!isDisableCheckExistsTeacherOnShift) {
            if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate,
                    null)) {
                return RouterHelper.responseError("Giảng viên dạy thay " + factory.getUserStaff().getName() + " - "
                        + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift()
                        + " trong ngày "
                        + DateTimeUtils.convertMillisToDate(startDate));
            }
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
                    "Không thể tạo ca do có sinh viên đang thuộc nhóm xưởng khác có cùng thời gian",
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

        // Enhanced logging with detailed information
        String logMessage = String.format(
                "vừa thêm mới ca ngày %s - Nhóm xưởng: %s - Kế hoạch: %s - Ca: %s - Thời gian: %s-%s",
                DateTimeUtils.convertMillisToDate(newEntity.getStartDate()),
                factory.getName(),
                plan.getName(),
                request.getShift(),
                DateTimeUtils.convertMillisToDate(startDate, "HH:mm"),
                DateTimeUtils.convertMillisToDate(endDate, "HH:mm"));
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Thêm mới ca thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> updateLinkMeet(SPDUpdateLinkMeetRequest request) {

        if (!ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link online không hợp lệ");
        }
        PlanFactory planFactory = spdPlanFactoryRepository.findById(request.getIdPlanFactory()).orElse(null);
        if (planFactory == null || !planFactory.getPlan().getProject().getSubjectFacility().getFacility().getId()
                .equals(sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy nhóm kế hoạch");
        }
        spdPlanDateRepository.updateAllLinkMeet(planFactory.getId(), request.getLink());

        // Enhanced logging with detailed information
        String logMessage = String.format(
                "vừa cập nhật link online cho nhóm xưởng: %s - Kế hoạch: %s - Link: %s",
                planFactory.getFactory().getName(),
                planFactory.getPlan().getName(),
                request.getLink());
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Cập nhật link online thành công");
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
            return RouterHelper.responseError("Không có ca nào sắp diễn ra");
        }

        byte[] file = createFileMail(lstPlanDate);
        if (file == null) {
            return RouterHelper.responseError("Không thể tạo tệp tin lịch");
        }

        MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
        mailerDefaultRequest.setTemplate(null);
        mailerDefaultRequest.setTo(planFactory.getFactory().getUserStaff().getEmail());
        mailerDefaultRequest
                .setTitle("Thông báo lịch sắp tới - " + planFactory.getFactory().getName() + " - " + appName);

        List<String> lstEmails = spdUserStudentRepository.getAllEmail(planFactory.getFactory().getId());

        if (!lstEmails.isEmpty()) {
            mailerDefaultRequest.setBcc(lstEmails.toArray(new String[0]));
        }

        Map<String, Object> dataMail = new HashMap<>();
        dataMail.put("STAFF_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());

        Map<String, byte[]> filesMail = new HashMap<>();
        filesMail.put("lich-sap-toi.xlsx", file);

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

    @Override
    public ResponseEntity<?> searchTeacher(SPDSearchTeacherRequest request) {
        List<SPDTeacherResponse> results = spdUserStaffRepository.getAllStaffByKeyword(sessionHelper.getFacilityId(), request.getKeyword());
        return RouterHelper.responseSuccess("Tìm thấy " + results.size() + " kết quả", results);
    }

}
