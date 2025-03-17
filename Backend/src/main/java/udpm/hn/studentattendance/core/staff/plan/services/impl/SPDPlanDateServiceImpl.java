package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateDetailRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateDetailResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftConstant;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SPDPlanDateServiceImpl implements SPDPlanDateService {

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDFactoryRepository spdFactoryRepository;

    private final SessionHelper sessionHelper;
    private final SPDPlanRepository sPDPlanRepository;

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateResponse> data = PageableObject.of(spdPlanDateRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getDetail(String idFactory) {
        Optional<SPDPlanDateResponse> data = spdPlanDateRepository.getDetailByIdFactory(idFactory, sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch nhóm xưởng"));
    }

    @Override
    public ResponseEntity<?> getAllDetailList(SPDFilterPlanDateDetailRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateDetailResponse> data = PageableObject.of(spdPlanDateRepository.getAllDetailByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePlanDate(String idPlanDate) {
        Optional<SPDPlanDateDetailResponse> entity = spdPlanDateRepository.getPlanDateById(idPlanDate, sessionHelper.getFacilityId());
        if (entity.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }
        if (spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(), entity.get().getId()) > 0) {
            return RouterHelper.responseSuccess("Xoá thành công kế hoạch chi tiết.");
        }

        return RouterHelper.responseError("Không thể xoá kế hoạch chi tiết này");
    }

    @Override
    public ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        SPDPlanDateDetailResponse entity = spdPlanDateRepository.getPlanDateById(request.getId(), request.getIdFacility()).orElse(null);

        if (entity == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }

        if (entity.getStatus().equalsIgnoreCase("Da_DIEN_RA")) {
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
            return RouterHelper.responseError("Ngày học diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < entity.getFromDate() || startDate > entity.getToDate()) {
            return RouterHelper.responseError("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(entity.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(entity.getToDate()));
        }

        PlanDate planDate = spdPlanDateRepository.findById(entity.getId()).orElse(null);
        if (planDate == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch chi tiết");
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getFactory().getId(), planDate.getId(), startDate, request.getShift())) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsShiftInPlanDate(planDate.getFactory().getUserStaff().getId(), planDate.getId(), startDate, request.getShift())) {
            return RouterHelper.responseError("Giảng viên " + planDate.getFactory().getUserStaff().getName() + " - " + planDate.getFactory().getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
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

        Plan plan = sPDPlanRepository.findById(request.getIdPlan()).orElse(null);

        if (plan == null
                || plan.getStatus() != EntityStatus.ACTIVE
                || plan.getProject().getStatus() != EntityStatus.ACTIVE
                || plan.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(), request.getIdFacility())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || factory.getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
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

        System.out.println(startDate);
        System.out.println(DateTimeUtils.getCurrentTimeMillis());

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Ngày học diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        Semester semester = factory.getProject().getSemester();

        if (startDate < semester.getFromDate() || startDate > semester.getToDate()) {
            return RouterHelper.responseError("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(semester.getToDate()));
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(factory.getId(), null, startDate, request.getShift())) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsShiftInPlanDate(factory.getUserStaff().getId(), null, startDate, request.getShift())) {
            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        PlanDate planDate = new PlanDate();
        planDate.setPlan(plan);
        planDate.setFactory(factory);
        planDate.setStartDate(startDate);
        planDate.setShift(request.getShift());
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        return RouterHelper.responseSuccess("Thêm mới kế hoạch thành công", spdPlanDateRepository.save(planDate));
    }

    @Override
    public ResponseEntity<?> addFactory(SPDAddFactoryRequest request) {

        Plan plan = sPDPlanRepository.findById(request.getIdPlan()).orElse(null);

        if (plan == null
                || plan.getStatus() != EntityStatus.ACTIVE
                || plan.getProject().getStatus() != EntityStatus.ACTIVE
                || plan.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(plan.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || factory.getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.responseError("Ca học không hợp lệ");
        }

        Long startDate = ShiftConstant.getShiftTimeStart(plan.getFromDate(), shift);
        Long endDate = ShiftConstant.getShiftTimeStart(plan.getToDate(), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Ngày bắt đầu diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (endDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Ngày kết thúc diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        Semester semester = factory.getProject().getSemester();

        if (startDate < semester.getFromDate() || startDate > semester.getToDate()) {
            return RouterHelper.responseError("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(semester.getToDate()));
        }

        List<PlanDate> lstPlanDate = new ArrayList<>();

        LocalDate fromDate = DateTimeUtils.convertTimestampToLocalDate(startDate);
        LocalDate toDate = DateTimeUtils.convertTimestampToLocalDate(endDate);

        LocalDate current = fromDate;

        while (!current.isAfter(toDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (request.getDays().contains(dayOfWeek.getValue())) {
                if (spdPlanDateRepository.isExistsShiftInFactory(factory.getId(), null, startDate, request.getShift())) {
                    return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
                }
                if (spdPlanDateRepository.isExistsShiftInPlanDate(factory.getUserStaff().getId(), null, startDate, request.getShift())) {
                    return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
                }
                PlanDate planDate = new PlanDate();
                planDate.setFactory(factory);
                planDate.setStartDate(ShiftConstant.getShiftTimeStart(current, shift));
                planDate.setShift(request.getShift());
                planDate.setDescription(null);
                planDate.setLateArrival(request.getLateArrival());
                lstPlanDate.add(planDate);
            }
            current = current.plusDays(1);
        }

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công " + lstPlanDate.size() + " kế hoạch", spdPlanDateRepository.saveAllAndFlush(lstPlanDate));
    }

}
