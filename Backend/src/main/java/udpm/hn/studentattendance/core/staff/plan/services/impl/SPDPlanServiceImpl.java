package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;

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
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonPlanDateRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class SPDPlanServiceImpl implements SPDPlanService {

    private final SPDPlanRepository spdPlanRepository;

    private final SPDSubjectRepository spdSubjectRepository;

    private final SPDLevelProjectRepository spdLevelProjectRepository;

    private final SPDSemesterRepository spdSemesterRepository;

    private final SPDProjectRepository spdProjectRepository;

    private final SessionHelper sessionHelper;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    private final CommonPlanDateRepository commonPlanDateRepository;

    public List<SPDSubjectResponse> getCachedSubjects() {
        String key = RedisPrefixConstant.REDIS_PREFIX_PLAN + "subjects_" + "facility=" + sessionHelper.getFacilityId();
        return redisCacheHelper.getOrSet(
                key,
                () -> spdSubjectRepository.getAllByFacility(sessionHelper.getFacilityId()),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getAllSubject() {
        List<SPDSubjectResponse> data = getCachedSubjects();
        return RouterHelper.responseSuccess("Lấy dữ liệu bộ môn thành công", data);
    }

    public List<SPDLevelProjectResponse> getCachedLevels() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "all";
        return redisCacheHelper.getOrSet(cacheKey, spdLevelProjectRepository::getAll, new TypeReference<>() {});
    }

    @Override
    public ResponseEntity<?> getAllLevel() {
        List<SPDLevelProjectResponse> data = getCachedLevels();
        return RouterHelper.responseSuccess("Lấy dữ liệu level thành công", data);
    }

    public List<String> getCachedSemesterNames() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "semester_names_all";

        return redisCacheHelper.getOrSet(
                cacheKey,
                () -> Arrays.stream(SemesterName.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()),
                new TypeReference<>() {});
    }

    @Override
    public ResponseEntity<?> getListSemester() {
        List<String> data = getCachedSemesterNames();
        return RouterHelper.responseSuccess("Lấy dữ liệu kỳ thành công", data);
    }

    public List<Integer> getCachedYears() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "years_all";
        return redisCacheHelper.getOrSet(cacheKey, spdSemesterRepository::getAllYear, new TypeReference<>() {});
    }

    @Override
    public ResponseEntity<?> getAllYear() {
        List<Integer> data = getCachedYears();
        return RouterHelper.responseSuccess("Lấy dữ liệu năm thành công", data);
    }

    public PageableObject<SPDPlanResponse> getCachedPlans(SPDFilterPlanRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        String key = RedisPrefixConstant.REDIS_PREFIX_PLAN + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject
                        .of(spdPlanRepository.getAllByFilter(PaginationHelper.createPageable(request), request)),
                new TypeReference<>() {});
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanRequest request) {
        PageableObject<SPDPlanResponse> data = getCachedPlans(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    public SPDPlanResponse getCachedPlanById(String idPlan) {
        Optional<SPDPlanResponse> data = spdPlanRepository.getByIdPlan(idPlan, sessionHelper.getFacilityId());
        return data.orElse(null);
    }

    @Override
    public ResponseEntity<?> getPlan(String idPlan) {
        SPDPlanResponse data = getCachedPlanById(idPlan);
        if (data != null) {
            return RouterHelper.responseSuccess("Get dữ liệu thành công", data);
        } else {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }
    }

    public List<SPDProjectResponse> getCachedProjects(SPDFilterCreatePlanRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "projects_" +
                "facility=" + sessionHelper.getFacilityId() + "_" + request.toString();

        return redisCacheHelper.getOrSet(
                cacheKey,
                () -> spdPlanRepository.getListProject(request),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getListProject(SPDFilterCreatePlanRequest request) {
        List<SPDProjectResponse> data = getCachedProjects(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> createPlan(SPDAddOrUpdatePlanRequest request) {

        Project project = spdProjectRepository.findById(request.getIdProject()).orElse(null);

        if (!ValidateHelper.isValidName(request.getName())) {
            return RouterHelper.responseError("Tên kế hoạch chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
        }

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
        plan.setMaxLateArrival(request.getMaxLateArrival());

        Plan o = spdPlanRepository.save(plan);
        userActivityLogHelper.saveLog("vừa thêm 1 kế hoạch mới: " + o.getName());

        // Invalidate related caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Tạo mới kế hoạch thành công", o);
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
            return RouterHelper.responseError("Không thể thay đổi trạng thái kế hoạch này. Vui lòng kiểm tra lại trạng thái dự án, nhóm dự án, ...");
        }

        if (commonPlanDateRepository.existsNotYetStartedByPlan(plan.getId())) {
            return RouterHelper.responseError("Đang tồn tại ca chưa hoặc đang diễn ra. Không thể thay đổi trạng thái");
        }

        if (plan.getStatus() == EntityStatus.INACTIVE
                && spdPlanRepository.isExistsProjectInPlan(plan.getProject().getId(), null)) {
            return RouterHelper.responseError(
                    "Dự án " + plan.getProject().getName() + " đã được triển khai trong một kế hoạch khác");
        }

        plan.setStatus(plan.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        Plan newEntity = spdPlanRepository.save(plan);

        // Invalidate specific cache for this plan
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái kế hoạch thành công", newEntity);
    }

    @Override
    public ResponseEntity<?> deletePlan(String id) {
        Plan plan = spdPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        SPDPlanResponse planResponse = spdPlanRepository.getByIdPlan(plan.getId(), sessionHelper.getFacilityId())
                .orElse(null);

        if (planResponse != null && planResponse.getStatus() != EntityStatus.INACTIVE.ordinal()) {
            return RouterHelper.responseError("Không thể xoá kế hoạch đang triển khai");
        }

        spdPlanRepository.deleteAllAttendanceByIdPlan(plan.getId());
        spdPlanRepository.deleteAllPlanDateByIdPlan(plan.getId());
        spdPlanRepository.deleteAllPlanFactoryByIdPlan(plan.getId());
        spdPlanRepository.delete(plan);
        userActivityLogHelper.saveLog("vừa xóa kế hoạch: " + plan.getName());

        // Invalidate specific cache for this plan
        redisInvalidationHelper.invalidateAllCaches();

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

        if (!ValidateHelper.isValidName(request.getName())) {
            return RouterHelper.responseError("Tên kế hoạch chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
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
        plan.setMaxLateArrival(request.getMaxLateArrival());

        Plan updatePlan = spdPlanRepository.save(plan);

        spdPlanRepository.deleteAllAttendanceOutRangeDateByIdPlan(updatePlan.getId());
        spdPlanRepository.deleteAllPlanDateOutRangeDateByIdPlan(updatePlan.getId());

        userActivityLogHelper.saveLog("vừa cập nhật kế hoạch: " + updatePlan.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", updatePlan);
    }

}
