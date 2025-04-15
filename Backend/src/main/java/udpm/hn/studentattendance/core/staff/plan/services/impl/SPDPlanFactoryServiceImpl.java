package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;
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

    private final SessionHelper sessionHelper;

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

        FacilityShift shift = spdFacilityShiftRepository.findByShiftAndFacility_Id(request.getShift(), sessionHelper.getFacilityId()).orElse(null);
        if (shift == null) {
            return RouterHelper.responseError("Ca học không tồn tại");
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
            Long date = ShiftHelper.getShiftTimeStart(current, LocalTime.of(shift.getFromHour(), shift.getFromMinute()));
            current = current.plusDays(1);

            if (request.getDays().contains(dayOfWeek.getValue())) {
                if (date < DateTimeUtils.getCurrentTimeMillis()) {
                    continue;
                }
                if (date < DateTimeUtils.getCurrentTimeMillis()) {
                    continue;
                }

                if (date < DateTimeUtils.getCurrentTimeMillis()) {
                    continue;
                }

                if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, date, request.getShift())) {
                    spdPlanFactoryRepository.delete(planFactory);
                    return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(date));
                }
                if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), date, request.getShift())) {
                    spdPlanFactoryRepository.delete(planFactory);
                    return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(date));
                }

                String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

                PlanDate planDate = new PlanDate();
                planDate.setPlanFactory(planFactory);
                planDate.setStartDate(date);
                planDate.setEndDate(date + ShiftHelper.getDiffTime(shift));
                planDate.setShift(request.getShift());
                planDate.setType(type);
                planDate.setLink(link);
                planDate.setRequiredIp(requiredIp);
                planDate.setRequiredLocation(requiredLocation);
                planDate.setDescription(null);
                planDate.setLateArrival(request.getLateArrival());
                lstPlanDate.add(planDate);
            }
        }

        if (lstPlanDate.isEmpty()) {
            spdPlanFactoryRepository.delete(planFactory);
            return RouterHelper.responseError("Không có ca học nào phù hợp trong khoảng thời gian diễn ra");
        }

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công " + lstPlanDate.size() + " kế hoạch", spdPlanDateRepository.saveAllAndFlush(lstPlanDate));
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
        return RouterHelper.responseSuccess("Thay đổi trạng thái kế hoạch thành công", spdPlanFactoryRepository.save(planFactory));
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
        List<FacilityShift> data = SPDFacilityShiftRepository.findAllByFacility_IdAndStatusOrderByShiftAsc(sessionHelper.getFacilityId(), EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
