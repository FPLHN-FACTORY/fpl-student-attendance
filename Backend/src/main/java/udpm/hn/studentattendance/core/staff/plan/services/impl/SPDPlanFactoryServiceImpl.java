package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDUserStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanFactoryService;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonPlanDateRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SPDPlanFactoryServiceImpl implements SPDPlanFactoryService {

    private final SPDPlanRepository spdPlanRepository;

    private final SPDPlanFactoryRepository spdPlanFactoryRepository;

    private final SPDFactoryRepository spdFactoryRepository;

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDFacilityShiftRepository SPDFacilityShiftRepository;

    private final SPDFacilityShiftRepository spdFacilityShiftRepository;

    private final SessionHelper sessionHelper;

    private final SettingHelper settingHelper;

    private final UserActivityLogHelper userActivityLogHelper;

    private final CommonPlanDateRepository commonPlanDateRepository;

    @Value("${app.config.allows-one-teacher-to-teach-multiple-classes}")
    private boolean isDisableCheckExistsTeacherOnShift;

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanFactoryRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanFactoryResponse> data = PageableObject
                .of(spdPlanFactoryRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListFactory(String idPlan) {
        Plan plan = spdPlanRepository.findById(idPlan).orElse(null);
        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(),
                        sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Kế hoạch không tồn tại hoặc đã bị xoá");
        }

        List<SPDFactoryResponse> data = spdPlanFactoryRepository.getListFactory(plan.getProject().getId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> createPlanFactory(SPDAddPlanFactoryRequest request) {

        int MAX_LATE_ARRIVAL = settingHelper.getSetting(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, Integer.class);

        if (request.getLateArrival() > MAX_LATE_ARRIVAL) {
            return RouterHelper.responseError("Thời gian điểm danh muộn nhất không quá " + MAX_LATE_ARRIVAL + " phút");
        }

        Plan plan = spdPlanRepository.findById(request.getIdPlan()).orElse(null);

        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(),
                        sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(),
                        sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

//        if (spdFactoryRepository.getCountTotalStudentInFactory(factory.getId()) > 0) {
//            return RouterHelper.responseError("Chỉ có thể thêm nhóm xưởng khi chưa có sinh viên nào");
//        }

        List<Integer> lstConsecutiveShift = request.getShift();
        if (lstConsecutiveShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca");
        }

        ShiftType type;
        try {
            type = ShiftType.fromKey(request.getType());
        } catch (Exception e) {
            return RouterHelper.responseError("Hình thức không hợp lệ");
        }


        if (type == ShiftType.ONLINE && !StringUtils.hasText(request.getLink())) {
            return RouterHelper.responseError("Link online không được bỏ trống");
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

        if (spdPlanFactoryRepository.isExistsFactoryInPlan(factory.getId())) {
            return RouterHelper
                    .responseError("Nhóm xưởng " + factory.getName() + " đã được triển khai trong kế hoạch này");
        }

        PlanFactory planFactory = spdPlanFactoryRepository.save(new PlanFactory(plan, factory));

        if (planFactory.getId() == null) {
            return RouterHelper.responseError("Không thể thêm nhóm xưởng vào kế hoạch");
        }

        List<PlanDate> lstPlanDate = new ArrayList<>();

        LocalDate fromDate = DateTimeUtils.convertTimestampToLocalDate(plan.getFromDate());
        LocalDate toDate = DateTimeUtils.convertTimestampToLocalDate(plan.getToDate());

        LocalDate current = fromDate;

        while (!current.isAfter(toDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            for (Integer shift : lstConsecutiveShift) {

                FacilityShift shiftStart = spdFacilityShiftRepository
                        .getOneById(shift, sessionHelper.getFacilityId()).orElse(null);

                if (shiftStart == null) {
                    return RouterHelper.responseError("Ca " + shift + " không tồn tại");
                }

                Long startDate = ShiftHelper.getShiftTimeStart(current,
                        LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
                Long endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(),
                        shiftStart.getToHour(), shiftStart.getToMinute());

                if (request.getDays().contains(dayOfWeek.getValue())) {
                    if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
                        continue;
                    }
                    if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
                        continue;
                    }

                    if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
                        continue;
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

                    if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, endDate)) {
                        spdPlanFactoryRepository.delete(planFactory);
                        return RouterHelper.responseError("Đã tồn tại ca diễn ra trong khoảng thời gian từ "
                                + DateTimeUtils.convertMillisToDate(startDate, "HH:mm") + " đến "
                                + DateTimeUtils.convertMillisToDate(endDate, "HH:mm") + " của ngày "
                                + DateTimeUtils.convertMillisToDate(startDate));
                    }

                    if (!isDisableCheckExistsTeacherOnShift) {
                        if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate,
                                endDate, null)) {
                            spdPlanFactoryRepository.delete(planFactory);
                            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - "
                                    + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift()
                                    + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
                        }
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
                    planDate.setShift(List.of(shift));
                    planDate.setType(type);
                    planDate.setLink(link);
                    planDate.setRoom(type == ShiftType.ONLINE ? null : request.getRoom());
                    planDate.setRequiredIp(requiredIp);
                    planDate.setRequiredLocation(requiredLocation);
                    planDate.setRequiredCheckin(requiredCheckin);
                    planDate.setRequiredCheckout(requiredCheckout);
                    planDate.setDescription(null);
                    planDate.setLateArrival(request.getLateArrival());
                    lstPlanDate.add(planDate);
                }
            }
            current = current.plusDays(1);
        }

        if (lstPlanDate.isEmpty()) {
            spdPlanFactoryRepository.delete(planFactory);
            return RouterHelper.responseError("Không có ca nào phù hợp trong khoảng thời gian diễn ra");
        }
        List<PlanDate> lstEntity = spdPlanDateRepository.saveAllAndFlush(lstPlanDate);

        String firstPlanDate = lstPlanDate.isEmpty() ? "Không có"
                : DateTimeUtils.convertMillisToDate(lstPlanDate.get(0).getStartDate(), "dd/MM/yyyy HH:mm");
        String lastPlanDate = lstPlanDate.isEmpty() ? "Không có"
                : DateTimeUtils.convertMillisToDate(lstPlanDate.get(lstPlanDate.size() - 1).getStartDate(),
                        "dd/MM/yyyy HH:mm");

        String logMessage = String.format(
                "vừa thêm nhóm xưởng '%s' vào kế hoạch '%s' - " +
                        "Hình thức: %s, Ca: %s, Ngày: %s, Phòng: %s, Link: %s, " +
                        "Thời gian muộn: %d phút, Tổng số buổi: %d, " +
                        "Thời gian từ: %s đến: %s",
                factory.getName(),
                plan.getName(),
                type.name(), request.getShift(), request.getDays(),
                request.getRoom() != null ? request.getRoom() : "Không có",
                request.getLink() != null ? "Có" : "Không",
                request.getLateArrival(), lstPlanDate.size(),
                firstPlanDate, lastPlanDate);
        userActivityLogHelper.saveLog(logMessage);

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công " + lstPlanDate.size() + " kế hoạch",
                lstEntity);
    }

    @Override
    public ResponseEntity<?> changeStatus(String idPlanFactory) {
        PlanFactory planFactory = spdPlanFactoryRepository.findById(idPlanFactory).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng trong kế hoạch này");
        }

        SPDPlanFactoryResponse planFactoryResponse = spdPlanFactoryRepository
                .getDetail(planFactory.getId(), sessionHelper.getFacilityId()).orElse(null);
        if (planFactoryResponse == null || planFactoryResponse.getStatus() != planFactory.getStatus().ordinal()) {
            return RouterHelper.responseError("Không thể thay đổi trạng thái nhóm xưởng này trong kế hoạch");
        }

        if (commonPlanDateRepository.existsNotYetStartedByPlanFactory(planFactory.getId())) {
            return RouterHelper.responseError("Đang tồn tại ca chưa hoặc đang diễn ra. Không thể thay đổi trạng thái");
        }

        if (planFactory.getStatus() == EntityStatus.INACTIVE
                && spdPlanFactoryRepository.isExistsFactoryInPlan(planFactory.getFactory().getId())) {
            return RouterHelper.responseError(
                    "Nhóm xưởng " + planFactory.getFactory().getName() + " đã được triển khai trong một kế hoạch này");
        }

        planFactory.setStatus(
                planFactory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        PlanFactory newEntity = spdPlanFactoryRepository.save(planFactory);

        String oldStatus = planFactory.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        String newStatus = newEntity.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        userActivityLogHelper.saveLog("vừa thay đổi trạng thái nhóm xưởng " + planFactory.getFactory().getName()
                + " trong kế hoạch " + planFactory.getPlan().getName() + " từ " + oldStatus + " thành " + newStatus);
        return RouterHelper.responseSuccess("Thay đổi trạng thái kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> deletePlanFactory(String idPlanFactory) {
        PlanFactory planFactory = spdPlanFactoryRepository.findById(idPlanFactory).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng trong kế hoạch này");
        }

        SPDPlanFactoryResponse planFactoryResponse = spdPlanFactoryRepository
                .getDetail(planFactory.getId(), sessionHelper.getFacilityId()).orElse(null);

        if (planFactoryResponse != null && planFactoryResponse.getStatus() != EntityStatus.INACTIVE.ordinal()) {
            return RouterHelper.responseError("Không thể xoá nhóm xưởng đang triển khai trong kế hoạch này");
        }

        spdPlanFactoryRepository.deleteAllAttendanceByIdPlanFactory(planFactory.getId());
        spdPlanFactoryRepository.deleteAllPlanDateByIdPlanFactory(planFactory.getId());
        spdPlanFactoryRepository.delete(planFactory);

        userActivityLogHelper.saveLog("vừa xóa nhóm xưởng " + planFactory.getFactory().getName() + " khỏi kế hoạch "
                + planFactory.getPlan().getName());
        return RouterHelper.responseSuccess("Xoá thành công nhóm xưởng ra khỏi kế hoạch");
    }

    @Override
    public ResponseEntity<?> getListShift() {
        List<FacilityShift> data = SPDFacilityShiftRepository.getAllList(sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
