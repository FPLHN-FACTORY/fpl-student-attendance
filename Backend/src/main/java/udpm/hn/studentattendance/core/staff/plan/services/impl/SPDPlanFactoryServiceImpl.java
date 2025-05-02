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
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;

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

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    @Value("${app.config.shift.max-late-arrival}")
    private int MAX_LATE_ARRIVAL;

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanFactoryRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanFactoryResponse> data = PageableObject.of(spdPlanFactoryRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListFactory(String idPlan) {
        Plan plan = spdPlanRepository.findById(idPlan).orElse(null);
        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Kế hoạch không tồn tại hoặc đã bị xoá");
        }

        List<SPDFactoryResponse> data = spdPlanFactoryRepository.getListFactory(plan.getProject().getId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> createPlanFactory(SPDAddPlanFactoryRequest request) {

        if (request.getLateArrival() > MAX_LATE_ARRIVAL) {
            return RouterHelper.responseError("Thời gian điểm danh muộn nhất không quá " + MAX_LATE_ARRIVAL + " phút");
        }

        Plan plan = spdPlanRepository.findById(request.getIdPlan()).orElse(null);

        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        List<List<Integer>> lstConsecutiveShift = ShiftHelper.findConsecutiveShift(request.getShift());
        if (lstConsecutiveShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca học");
        }

        ShiftType type;
        try {
            type = ShiftType.fromKey(request.getType());
        } catch (Exception e) {
            return RouterHelper.responseError("Hinh thức học không hợp lệ");
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link học online không hợp lệ");
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        if (requiredIp == null || requiredLocation == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        if (spdPlanFactoryRepository.isExistsFactoryInPlan(factory.getId())) {
            return RouterHelper.responseError("Nhóm xưởng " + factory.getName() + " đã được triển khai trong một kế hoạch này");
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
            for (List<Integer> lstShift : lstConsecutiveShift) {
                int startShift = lstShift.get(0);
                int endShift = lstShift.get(lstShift.size() - 1);

                FacilityShift shiftStart = spdFacilityShiftRepository.getOneById(startShift, sessionHelper.getFacilityId()).orElse(null);
                FacilityShift shiftEnd = spdFacilityShiftRepository.getOneById(endShift, sessionHelper.getFacilityId()).orElse(null);
                if (shiftStart == null) {
                    return RouterHelper.responseError("Ca học " + startShift + " không tồn tại");
                }
                if (shiftEnd == null) {
                    return RouterHelper.responseError("Ca học " + endShift + " không tồn tại");
                }
                Long startDate = ShiftHelper.getShiftTimeStart(current, LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
                Long endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(), shiftEnd.getToHour(), shiftEnd.getToMinute());

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

                    if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, endDate)) {
                        spdPlanFactoryRepository.delete(planFactory);
                        return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
                    }
                    if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate)) {
                        spdPlanFactoryRepository.delete(planFactory);
                        return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
                    }

                    String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

                    PlanDate planDate = new PlanDate();
                    planDate.setPlanFactory(planFactory);
                    planDate.setStartDate(startDate);
                    planDate.setEndDate(endDate);
                    planDate.setShift(lstShift);
                    planDate.setType(type);
                    planDate.setLink(link);
                    planDate.setRequiredIp(requiredIp);
                    planDate.setRequiredLocation(requiredLocation);
                    planDate.setDescription(null);
                    planDate.setLateArrival(request.getLateArrival());
                    lstPlanDate.add(planDate);
                }
            }
            current = current.plusDays(1);
        }

        if (lstPlanDate.isEmpty()) {
            spdPlanFactoryRepository.delete(planFactory);
            return RouterHelper.responseError("Không có ca học nào phù hợp trong khoảng thời gian diễn ra");
        }
        List<PlanDate> lstEntity = spdPlanDateRepository.saveAllAndFlush(lstPlanDate);

        commonUserStudentRepository.disableAllStudentDuplicateShiftByIdPlanFactory(planFactory.getId());

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công " + lstPlanDate.size() + " kế hoạch", lstEntity);
    }

    @Override
    public ResponseEntity<?> changeStatus(String idPlanFactory) {
        PlanFactory planFactory = spdPlanFactoryRepository.findById(idPlanFactory).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng trong kế hoạch này");
        }

        SPDPlanFactoryResponse planFactoryResponse = spdPlanFactoryRepository.getDetail(planFactory.getId(), sessionHelper.getFacilityId()).orElse(null);
        if (planFactoryResponse == null || planFactoryResponse.getStatus() != planFactory.getStatus().ordinal()) {
            return RouterHelper.responseError("Không thể thay đổi trạng thái nhóm xưởng này trong kế hoạch");
        }

        if (planFactory.getStatus() == EntityStatus.INACTIVE && spdPlanFactoryRepository.isExistsFactoryInPlan(planFactory.getFactory().getId())) {
            return RouterHelper.responseError("Nhóm xưởng " + planFactory.getFactory().getName() + " đã được triển khai trong một kế hoạch này");
        }

        planFactory.setStatus(planFactory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        PlanFactory newEntity = spdPlanFactoryRepository.save(planFactory);

        if (newEntity.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdPlanFactory(planFactory.getId());
        }

        return RouterHelper.responseSuccess("Thay đổi trạng thái kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> deletePlanFactory(String idPlanFactory) {
        PlanFactory planFactory = spdPlanFactoryRepository.findById(idPlanFactory).orElse(null);
        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng trong kế hoạch này");
        }

        if (planFactory.getStatus() != EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không thể xoá nhóm xưởng đang triển khai trong kế hoạch này");
        }

        spdPlanFactoryRepository.deleteAllAttendanceByIdPlanFactory(planFactory.getId());
        spdPlanFactoryRepository.deleteAllPlanDateByIdPlanFactory(planFactory.getId());
        spdPlanFactoryRepository.delete(planFactory);
        return RouterHelper.responseSuccess("Xoá thành công nhóm xưởng ra khỏi kế hoạch");
    }

    @Override
    public ResponseEntity<?> getListShift() {
        List<FacilityShift> data = SPDFacilityShiftRepository.getAllList(sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
