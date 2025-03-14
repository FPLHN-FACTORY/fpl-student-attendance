package udpm.hn.studentattendance.core.staff.plan.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateDetailRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateDetailResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDSubjectResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFactoryRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDLevelProjectRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDSemesterRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDSubjectRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.infrastructure.constants.ShiftConstant;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SPDPlanDateServiceImpl implements SPDPlanDateService {

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDSubjectRepository spdSubjectRepository;

    private final SPDLevelProjectRepository spdLevelProjectRepository;

    private final SPDSemesterRepository spdSemesterRepository;

    private final SPDFactoryRepository spdFactoryRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllSubject() {
        List<SPDSubjectResponse> data = spdSubjectRepository.getAllByFacility(sessionHelper.getFacilityId());
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy dữ liệu bộ môn thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllLevel() {
        List<SPDLevelProjectResponse> data = spdLevelProjectRepository.getAll();
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy dữ liệu level thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getListSemester() {
        List<String> data = Arrays.stream(SemesterName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy dữ liệu học kỳ thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllYear() {
        List<Integer> data = spdSemesterRepository.getAllYear();
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy dữ liệu năm học thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateResponse> data = PageableObject.of(spdPlanDateRepository.getAllByFilter(pageable, request));
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy danh sách dữ liệu thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getDetail(String idFactory) {
        Optional<SPDPlanDateResponse> data = spdPlanDateRepository.getDetailByIdFactory(idFactory, sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.createResponseApi(ApiResponse.success("Get dữ liệu thành công", spdPlanDateResponse), HttpStatus.OK))
                .orElseGet(() -> RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy kế hoạch nhóm xưởng"), HttpStatus.BAD_REQUEST));
    }

    @Override
    public ResponseEntity<?> getAllDetailList(SPDFilterPlanDateDetailRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateDetailResponse> data = PageableObject.of(spdPlanDateRepository.getAllDetailByFilter(pageable, request));
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy danh sách dữ liệu thành công", data), HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePlanDate(String idPlanDate) {
        Optional<SPDPlanDateDetailResponse> entity = spdPlanDateRepository.getPlanDateById(idPlanDate, sessionHelper.getFacilityId());
        if (entity.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy kế hoạch chi tiết"), HttpStatus.BAD_REQUEST);
        }
        if (spdPlanDateRepository.deletePlanDateById(sessionHelper.getFacilityId(), entity.get().getId()) > 0) {
            return RouterHelper.createResponseApi(ApiResponse.success("Xoá thành công kế hoạch chi tiết."), HttpStatus.OK);
        }

        return RouterHelper.createResponseApi(ApiResponse.error("Không thể xoá kế hoạch chi tiết này"), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        SPDPlanDateDetailResponse entity = spdPlanDateRepository.getPlanDateById(request.getId(), request.getIdFacility()).orElse(null);

        if (entity == null) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy kế hoạch chi tiết"), HttpStatus.BAD_REQUEST);
        }

        if (entity.getStatus().equalsIgnoreCase("Da_DIEN_RA")) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không thể cập nhật kế hoạch đã diễn ra"), HttpStatus.BAD_REQUEST);
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ca học không hợp lệ"), HttpStatus.BAD_REQUEST);
        }

        Long startDate = ShiftConstant.getShiftTimeStart(request.getStartDate(), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày học diễn ra phải lớn hơn hoặc bằng ngày hiện tại"), HttpStatus.BAD_REQUEST);
        }

        if (startDate < entity.getFromDate() || startDate > entity.getToDate()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(entity.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(entity.getToDate())), HttpStatus.BAD_REQUEST);
        }

        PlanDate planDate = spdPlanDateRepository.findById(entity.getId()).orElse(null);
        if (planDate == null) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy kế hoạch chi tiết"), HttpStatus.BAD_REQUEST);
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(planDate.getFactory().getId(), planDate.getId(), startDate, request.getShift())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
        }

        if (spdPlanDateRepository.isExistsShiftInPlanDate(planDate.getFactory().getUserStaff().getId(), planDate.getId(), startDate, request.getShift())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Giảng viên " + planDate.getFactory().getUserStaff().getName() + " - " + planDate.getFactory().getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
        }

        planDate.setStartDate(startDate);
        planDate.setShift(request.getShift());
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        return RouterHelper.createResponseApi(ApiResponse.success("Cập nhật kế hoạch thành công", spdPlanDateRepository.save(planDate)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> addPlanDate(SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || factory.getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(), request.getIdFacility())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy nhóm xưởng"), HttpStatus.BAD_REQUEST);
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ca học không hợp lệ"), HttpStatus.BAD_REQUEST);
        }

        Long startDate = ShiftConstant.getShiftTimeStart(request.getStartDate(), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày học diễn ra phải lớn hơn hoặc bằng ngày hiện tại"), HttpStatus.BAD_REQUEST);
        }

        Semester semester = factory.getProject().getSemester();

        if (startDate < semester.getFromDate() || startDate > semester.getToDate()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(semester.getToDate())), HttpStatus.BAD_REQUEST);
        }

        if (spdPlanDateRepository.isExistsShiftInFactory(factory.getId(), null, startDate, request.getShift())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
        }

        if (spdPlanDateRepository.isExistsShiftInPlanDate(factory.getUserStaff().getId(), null, startDate, request.getShift())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
        }

        PlanDate planDate = new PlanDate();
        planDate.setFactory(factory);
        planDate.setStartDate(startDate);
        planDate.setShift(request.getShift());
        planDate.setDescription(request.getDescription());
        planDate.setLateArrival(request.getLateArrival());

        return RouterHelper.createResponseApi(ApiResponse.success("Thêm mới kế hoạch thành công", spdPlanDateRepository.save(planDate)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getListFactory(SPDFilterCreatePlanDateRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        List<SPDPlanDateResponse> data = spdPlanDateRepository.getListFactory(request);
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy danh sách dữ liệu thành công", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createPlanDate(SPDCreatePlanDateRequest request) {

        Factory factory = spdFactoryRepository.findById(request.getIdFactory()).orElse(null);

        if (factory == null
                || factory.getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getStatus() != EntityStatus.ACTIVE
                || factory.getProject().getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(factory.getProject().getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.createResponseApi(ApiResponse.error("Không tìm thấy nhóm xưởng"), HttpStatus.BAD_REQUEST);
        }

        ShiftConstant shift;
        try {
            shift = ShiftConstant.valueOf("CA" + request.getShift());
        } catch (Exception e) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ca học không hợp lệ"), HttpStatus.BAD_REQUEST);
        }

        Long startDate = ShiftConstant.getShiftTimeStart(request.getRangeDate().get(0), shift);
        Long endDate = ShiftConstant.getShiftTimeStart(request.getRangeDate().get(1), shift);

        if (startDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày bắt đầu diễn ra phải lớn hơn hoặc bằng ngày hiện tại"), HttpStatus.BAD_REQUEST);
        }

        if (endDate < DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày kết thúc diễn ra phải lớn hơn hoặc bằng ngày hiện tại"), HttpStatus.BAD_REQUEST);
        }

        Semester semester = factory.getProject().getSemester();

        if (startDate < semester.getFromDate() || startDate > semester.getToDate()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Ngày học diễn ra phải trong khoảng từ " + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến " + DateTimeUtils.convertMillisToDate(semester.getToDate())), HttpStatus.BAD_REQUEST);
        }

        List<PlanDate> lstPlanDate = new ArrayList<>();

        LocalDate fromDate = DateTimeUtils.convertTimestampToLocalDate(startDate);
        LocalDate toDate = DateTimeUtils.convertTimestampToLocalDate(endDate);

        LocalDate current = fromDate;

        while (!current.isAfter(toDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (request.getDays().contains(dayOfWeek.getValue())) {
                if (spdPlanDateRepository.isExistsShiftInFactory(factory.getId(), null, startDate, request.getShift())) {
                    return RouterHelper.createResponseApi(ApiResponse.error("Đã tồn tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
                }
                if (spdPlanDateRepository.isExistsShiftInPlanDate(factory.getUserStaff().getId(), null, startDate, request.getShift())) {
                    return RouterHelper.createResponseApi(ApiResponse.error("Giảng viên " + factory.getUserStaff().getName() + " - " + factory.getUserStaff().getCode() + " đã đứng lớp tại ca " + request.getShift() + " trong ngày " + DateTimeUtils.convertMillisToDate(startDate)), HttpStatus.BAD_REQUEST);
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

        return RouterHelper.createResponseApi(ApiResponse.success("Tạo mới kế hoạch thành công " + lstPlanDate.size() + " kế hoạch", spdPlanDateRepository.saveAllAndFlush(lstPlanDate)), HttpStatus.OK);
    }

}
