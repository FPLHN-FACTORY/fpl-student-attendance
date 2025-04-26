package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
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
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SPDPlanDateServiceImpl implements SPDPlanDateService {

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDPlanFactoryRepository spdPlanFactoryRepository;

    private final SPDFacilityShiftRepository spdFacilityShiftRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

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

        if (DateTimeUtils.getCurrentTimeMillis() > planDate.getEndDate() ) {
            return RouterHelper.responseError("Không thể cập nhật kế hoạch đã diễn ra");
        }

        List<List<Integer>> lstShift = ShiftHelper.findConsecutiveShift(request.getShift());

        if (lstShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca học");
        }

        int startShift = lstShift.get(0).get(0);
        int endShift = lstShift.get(0).get(lstShift.get(0).size() - 1);

        FacilityShift shiftStart = spdFacilityShiftRepository.getOneById(startShift, sessionHelper.getFacilityId()).orElse(null);
        FacilityShift shiftEnd = spdFacilityShiftRepository.getOneById(endShift, sessionHelper.getFacilityId()).orElse(null);
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

        Long startDate = ShiftHelper.getShiftTimeStart(request.getStartDate(), LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
        Long endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(), shiftEnd.getToHour(), shiftEnd.getToMinute());

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < planDate.getPlanFactory().getPlan().getFromDate() || startDate > planDate.getPlanFactory().getPlan().getToDate()) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(planDate.getPlanFactory().getPlan().getToDate()));
        }

        Factory factory = planDate.getPlanFactory().getFactory();

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getPlanFactory().getId(), planDate.getId(), startDate, endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate)) {
            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link học online không hợp lệ");
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        if (requiredIp == null || requiredLocation == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

        planDate.setStartDate(startDate);
        planDate.setEndDate(endDate);
        planDate.setShift(request.getShift());
        planDate.setType(type);
        planDate.setLink(link);
        planDate.setRequiredIp(requiredIp);
        planDate.setRequiredLocation(requiredLocation);
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        PlanDate newEntity = spdPlanDateRepository.save(planDate);

        commonUserStudentRepository.disableAllStudentDuplicateShiftByStartDate(planDate.getPlanFactory().getFactory().getId(), planDate.getStartDate());

        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", newEntity);
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

        List<List<Integer>> lstShift = ShiftHelper.findConsecutiveShift(request.getShift());

        if (lstShift.isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 ca học");
        }

        int startShift = lstShift.get(0).get(0);
        int endShift = lstShift.get(0).get(lstShift.get(0).size() - 1);

        FacilityShift shiftStart = spdFacilityShiftRepository.getOneById(startShift, sessionHelper.getFacilityId()).orElse(null);
        FacilityShift shiftEnd = spdFacilityShiftRepository.getOneById(endShift, sessionHelper.getFacilityId()).orElse(null);
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

        Long startDate = ShiftHelper.getShiftTimeStart(request.getStartDate(), LocalTime.of(shiftStart.getFromHour(), shiftStart.getFromMinute()));
        Long endDate = startDate + ShiftHelper.getDiffTime(shiftStart.getFromHour(), shiftStart.getFromMinute(), shiftEnd.getToHour(), shiftEnd.getToMinute());

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Thời gian diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (startDate < plan.getFromDate() || startDate > plan.getToDate()) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(plan.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(plan.getToDate()));
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planFactory.getId(), null, startDate, endDate)) {
            return RouterHelper.responseError("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (spdPlanDateRepository.isExistsTeacherOnShift(factory.getUserStaff().getId(), startDate, endDate)) {
            return RouterHelper.responseError("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate));
        }

        if (StringUtils.hasText(request.getLink()) && !ValidateHelper.isValidURL(request.getLink())) {
            return RouterHelper.responseError("Link học online không hợp lệ");
        }

        StatusType requiredIp = StatusType.fromKey(request.getRequiredIp());
        StatusType requiredLocation = StatusType.fromKey(request.getRequiredLocation());
        if (requiredIp == null || requiredLocation == null) {
            return RouterHelper.responseError("Điều kiện điểm danh không hợp lệ");
        }

        String link = StringUtils.hasText(request.getLink()) ? request.getLink().trim() : null;

        PlanDate planDate = new PlanDate();
        planDate.setPlanFactory(planFactory);
        planDate.setStartDate(startDate);
        planDate.setEndDate(endDate);
        planDate.setShift(request.getShift());
        planDate.setType(type);
        planDate.setLink(link);
        planDate.setRequiredIp(requiredIp);
        planDate.setRequiredLocation(requiredLocation);
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        PlanDate newEntity = spdPlanDateRepository.save(planDate);

        commonUserStudentRepository.disableAllStudentDuplicateShiftByStartDate(planDate.getPlanFactory().getFactory().getId(), planDate.getStartDate());

        return RouterHelper.responseSuccess("Thêm mới kế hoạch thành công", newEntity);
    }

}
