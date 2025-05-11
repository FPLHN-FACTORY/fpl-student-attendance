package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDSubjectResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDLevelProjectRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDProjectRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDSemesterRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDSubjectRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanService;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SPDPlanServiceImpl implements SPDPlanService {

    private final SPDPlanRepository spdPlanRepository;

    private final SPDSubjectRepository spdSubjectRepository;

    private final SPDLevelProjectRepository spdLevelProjectRepository;

    private final SPDSemesterRepository spdSemesterRepository;

    private final SPDProjectRepository spdProjectRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllSubject() {
        List<SPDSubjectResponse> data = spdSubjectRepository.getAllByFacility(sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy dữ liệu bộ môn thành công", data);
    }

    @Override
    public ResponseEntity<?> getAllLevel() {
        List<SPDLevelProjectResponse> data = spdLevelProjectRepository.getAll();
        return RouterHelper.responseSuccess("Lấy dữ liệu level thành công", data);
    }

    @Override
    public ResponseEntity<?> getListSemester() {
        List<String> data = Arrays.stream(SemesterName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return RouterHelper.responseSuccess("Lấy dữ liệu học kỳ thành công", data);
    }

    @Override
    public ResponseEntity<?> getAllYear() {
        List<Integer> data = spdSemesterRepository.getAllYear();
        return RouterHelper.responseSuccess("Lấy dữ liệu năm học thành công", data);
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanResponse> data = PageableObject.of(spdPlanRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getPlan(String idPlan) {
        Optional<SPDPlanResponse> data = spdPlanRepository.getByIdPlan(idPlan, sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getListProject(SPDFilterCreatePlanRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        List<SPDProjectResponse> data = spdPlanRepository.getListProject(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> createPlan(SPDAddOrUpdatePlanRequest request) {

        Project project = spdProjectRepository.findById(request.getIdProject()).orElse(null);

        if (project == null
                || project.getStatus() != EntityStatus.ACTIVE
                || project.getSemester().getStatus() != EntityStatus.ACTIVE
                || !Objects.equals(project.getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy dự án");
        }

        if (spdPlanRepository.isExistsProjectInPlan(project.getId(), null)) {
            return RouterHelper
                    .responseError("Dự án " + project.getName() + " đã được triển khai trong 1 kế hoạch khác");
        }

        long startDate = DateTimeUtils.toStartOfDay(request.getRangeDate().get(0));
        long endDate = DateTimeUtils.toEndOfDay(request.getRangeDate().get(1));

        if (startDate < DateTimeUtils.toStartOfDay(DateTimeUtils.getCurrentTimeMillis())) {
            return RouterHelper.responseError("Ngày bắt đầu diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (endDate < DateTimeUtils.toStartOfDay(DateTimeUtils.getCurrentTimeMillis())) {
            return RouterHelper.responseError("Ngày kết thúc diễn ra phải lớn hơn hoặc bằng ngày hiện tại");
        }

        Semester semester = project.getSemester();

        if (startDate < DateTimeUtils.toStartOfDay(semester.getFromDate())
                || startDate > DateTimeUtils.toEndOfDay(semester.getToDate())) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ "
                    + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến "
                    + DateTimeUtils.convertMillisToDate(semester.getToDate()));
        }

        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setFromDate(DateTimeUtils.toStartOfDay(startDate));
        plan.setToDate(DateTimeUtils.toEndOfDay(endDate));
        plan.setProject(project);

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công", spdPlanRepository.save(plan));
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Plan plan = spdPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        SPDPlanResponse planResponse = spdPlanRepository.getByIdPlan(plan.getId(), sessionHelper.getFacilityId())
                .orElse(null);

        if (planResponse == null || planResponse.getStatus() != plan.getStatus().ordinal()) {
            return RouterHelper.responseError("Không thể thay đổi trạng thái kế hoạch này");
        }

        if (plan.getStatus() == EntityStatus.INACTIVE
                && spdPlanRepository.isExistsProjectInPlan(plan.getProject().getId(), null)) {
            return RouterHelper.responseError(
                    "Dự án " + plan.getProject().getName() + " đã được triển khai trong một kế hoạch khác");
        }

        plan.setStatus(plan.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        Plan newEntity = spdPlanRepository.save(plan);

        if (newEntity.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdPlan(plan.getId());
        }

        return RouterHelper.responseSuccess("Thay đổi trạng thái kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> deletePlan(String id) {
        Plan plan = spdPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        if (plan.getStatus() != EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không thể xoá kế hoạch đang triển khai");
        }

        spdPlanRepository.deleteAllAttendanceByIdPlan(plan.getId());
        spdPlanRepository.deleteAllPlanDateByIdPlan(plan.getId());
        spdPlanRepository.deleteAllPlanFactoryByIdPlan(plan.getId());
        spdPlanRepository.delete(plan);
        return RouterHelper.responseSuccess("Xoá thành công kế hoạch: " + plan.getName());
    }

    @Override
    public ResponseEntity<?> updatePlan(SPDAddOrUpdatePlanRequest request) {
        Plan plan = spdPlanRepository.findById(request.getId()).orElse(null);
        if (plan == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch muốn cập nhật");
        }

        Project project = spdProjectRepository.findById(request.getIdProject()).orElse(null);

        if (project == null
                || !Objects.equals(project.getSubjectFacility().getFacility().getId(), sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy dự án");
        }

        if (plan.getStatus() == EntityStatus.ACTIVE
                && spdPlanRepository.isExistsProjectInPlan(project.getId(), plan.getId())) {
            return RouterHelper
                    .responseError("Dự án " + project.getName() + " đã được triển khai trong 1 kế hoạch khác");
        }

        long startDate = DateTimeUtils.toStartOfDay(request.getRangeDate().get(0));
        long endDate = DateTimeUtils.toEndOfDay(request.getRangeDate().get(1));

        Semester semester = project.getSemester();

        if (startDate < DateTimeUtils.toStartOfDay(semester.getFromDate())
                || startDate > DateTimeUtils.toEndOfDay(semester.getToDate())) {
            return RouterHelper.responseError("Thời gian diễn ra phải trong khoảng từ "
                    + DateTimeUtils.convertMillisToDate(semester.getFromDate()) + " đến "
                    + DateTimeUtils.convertMillisToDate(semester.getToDate()));
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setFromDate(DateTimeUtils.toStartOfDay(startDate));
        plan.setToDate(DateTimeUtils.toEndOfDay(endDate));
        plan.setProject(project);

        Plan updatePlan = spdPlanRepository.save(plan);

        spdPlanRepository.deleteAllAttendanceOutRangeDateByIdPlan(updatePlan.getId());
        spdPlanRepository.deleteAllPlanDateOutRangeDateByIdPlan(updatePlan.getId());

        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", updatePlan);
    }

}
