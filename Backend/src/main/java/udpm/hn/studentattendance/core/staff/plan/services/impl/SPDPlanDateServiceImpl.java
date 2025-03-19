package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.ShiftConstant;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SPDPlanDateServiceImpl implements SPDPlanDateService {

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDPlanFactoryRepository spdPlanFactoryRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getDetail(String idPlanFactory) {
        Optional<SPDPlanFactoryResponse> data = spdPlanFactoryRepository.getDetail(idPlanFactory, sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateResponse> data = PageableObject.of(spdPlanDateRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePlanDate(String idPlanDate) {
        Optional<SPDPlanDateResponse> entity = spdPlanDateRepository.getPlanDateById(idPlanDate, sessionHelper.getFacilityId());
        if (entity.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }
        if (spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(), List.of(entity.get().getId())) > 0) {
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
            return RouterHelper.responseSuccess("Xoá thành công " + result + " kế hoạch chi tiết.");
        }
        return RouterHelper.responseError("Không có kế hoạch nào cần xoá");
    }

    @Override
    public ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        PlanDate planDate = spdPlanDateRepository.findById(request.getId()).orElse(null);

        if (planDate == null
                || !Objects.equals(planDate.getPlanFactory().getFactory().getProject().getSubjectFacility().getFacility().getId(), request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }

        if (DateTimeUtils.getCurrentTimeMillis() > planDate.getStartDate() + 7200 ) {
            return RouterHelper.responseError("Không thể cập nhật kế hoạch đã diễn ra");
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.responseError("Ca học không hợp lệ");
        }

        Long startDate = ShiftConstant.getShiftTimeStart(request.getStartDate(), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < planDate.getPlanFactory().getPlan().getFromDate() || startDate > planDate.getPlanFactory().getPlan().getToDate()) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getToDate()));
        }

        Factory factory = planDate.getPlanFactory().getFactory();

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getPlanFactory().getId(), planDate.getId(), startDate, request.getShift())) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, request.getShift())) {
            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        planDate.setStartDate(startDate);
        planDate.setShift(request.getShift());
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", spdPlanDateRepository.save(planDate));
    }

    @Override
    public ResponseEntity<?> addPlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        PlanFactory planFactory = spdPlanFactoryRepository.findById(request.getIdPlanFactory()).orElse(null);

        if (planFactory == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch nhóm xưởng");
        }

        Plan plan = planFactory.getPlan();

        if (plan == null
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(), request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = planFactory.getFactory();

        if (factory == null
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(), request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.responseError("Ca học không hợp lệ");
        }

        Long startDate = ShiftConstant.getShiftTimeStart(request.getStartDate(), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < plan.getFromDate() || startDate > plan.getToDate()) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(plan.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(plan.getToDate()));
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, request.getShift())) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, request.getShift())) {
            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        PlanDate planDate = new PlanDate();
        planDate.setPlanFactory(planFactory);
        planDate.setStartDate(startDate);
        planDate.setShift(request.getShift());
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        return RouterHelper.responseSuccess("Thêm mới kế hoạch thành công", spdPlanDateRepository.save(planDate));
    }

}
