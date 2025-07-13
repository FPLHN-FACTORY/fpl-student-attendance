package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

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

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public List<SPDSubjectResponse> getCachedSubjects() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "subjects_" + "facility="
                + sessionHelper.getFacilityId();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<SPDSubjectResponse> data = spdSubjectRepository.getAllByFacility(sessionHelper.getFacilityId());

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getAllSubject() {
        List<SPDSubjectResponse> data = getCachedSubjects();
        return RouterHelper.responseSuccess("Lấy dữ liệu bộ môn thành công", data);
    }

    public List<SPDLevelProjectResponse> getCachedLevels() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "all";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<SPDLevelProjectResponse> data = spdLevelProjectRepository.getAll();

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getAllLevel() {
        List<SPDLevelProjectResponse> data = getCachedLevels();
        return RouterHelper.responseSuccess("Lấy dữ liệu level thành công", data);
    }

    public List<String> getCachedSemesterNames() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "semester_names_all";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<String> data = Arrays.stream(SemesterName.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getListSemester() {
        List<String> data = getCachedSemesterNames();
        return RouterHelper.responseSuccess("Lấy dữ liệu học kỳ thành công", data);
    }

    public List<Integer> getCachedYears() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "years_all";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<Integer> data = spdSemesterRepository.getAllYear();

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getAllYear() {
        List<Integer> data = getCachedYears();
        return RouterHelper.responseSuccess("Lấy dữ liệu năm học thành công", data);
    }

    public PageableObject<SPDPlanResponse> getCachedPlans(SPDFilterPlanRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + "list_" + request.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedMap = redisService.getObject(cacheKey, Map.class);
                if (cachedMap != null) {
                    return convertMapToPageableObject(cachedMap);
                }
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanResponse> data = PageableObject.of(spdPlanRepository.getAllByFilter(pageable, request));

        try {
            Map<String, Object> cacheMap = convertPageableObjectToMap(data);
            redisService.set(cacheKey, cacheMap, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanRequest request) {
        PageableObject<SPDPlanResponse> data = getCachedPlans(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

        public SPDPlanResponse getCachedPlanById(String idPlan) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_PLAN + idPlan + "_facility=" + sessionHelper.getFacilityId();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                // Try to get as Map and convert
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedMap = redisService.getObject(cacheKey, Map.class);
                if (cachedMap != null) {
                    // Convert Map back to SPDPlanResponse
                    return convertMapToSPDPlanResponse(cachedMap);
                }
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Optional<SPDPlanResponse> data = spdPlanRepository.getByIdPlan(idPlan, sessionHelper.getFacilityId());

        SPDPlanResponse result = data.orElse(null);
        if (result != null) {
            try {
                // Cache as Map instead of interface to avoid deserialization issues
                Map<String, Object> cacheMap = convertSPDPlanResponseToMap(result);
                redisService.set(cacheKey, cacheMap, redisTTL);
            } catch (Exception ignored) {
            }
        }

        return result;
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

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<SPDProjectResponse> data = spdPlanRepository.getListProject(request);

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getListProject(SPDFilterCreatePlanRequest request) {
        List<SPDProjectResponse> data = getCachedProjects(request);
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
        plan.setMaxLateArrival(request.getMaxLateArrival());

        Plan o = spdPlanRepository.save(plan);
        userActivityLogHelper.saveLog("vừa thêm 1 kế hoạch mới: " + o.getName());

        // Invalidate related caches
        invalidatePlanCaches();

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

        // Invalidate specific cache for this plan
        invalidatePlanCache(id);

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
        userActivityLogHelper.saveLog("vừa xóa kế hoạch: " + plan.getName());

        // Invalidate specific cache for this plan
        invalidatePlanCache(id);

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
        plan.setMaxLateArrival(request.getMaxLateArrival());

        Plan updatePlan = spdPlanRepository.save(plan);

        spdPlanRepository.deleteAllAttendanceOutRangeDateByIdPlan(updatePlan.getId());
        spdPlanRepository.deleteAllPlanDateOutRangeDateByIdPlan(updatePlan.getId());

        userActivityLogHelper.saveLog("vừa cập nhật kế hoạch: " + updatePlan.getName());

        // Invalidate specific cache for this plan
        invalidatePlanCache(request.getId());

        return RouterHelper.responseSuccess("Cập nhật kế hoạch thành công", updatePlan);
    }

    private void invalidatePlanCache(String planId) {
        redisInvalidationHelper.invalidateAllCaches();
    }

    private void invalidatePlanCaches() {
        redisInvalidationHelper.invalidateAllCaches();
    }

    // Helper methods to convert between Map and objects
    private Map<String, Object> convertPageableObjectToMap(PageableObject<SPDPlanResponse> pageableObject) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", pageableObject.getData().stream()
                .map(this::convertSPDPlanResponseToMap)
                .collect(Collectors.toList()));
        map.put("totalPages", pageableObject.getTotalPages());
        map.put("currentPage", pageableObject.getCurrentPage());
        return map;
    }

    private PageableObject<SPDPlanResponse> convertMapToPageableObject(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataMaps = (List<Map<String, Object>>) map.get("data");
        List<SPDPlanResponse> data = dataMaps.stream()
                .map(this::convertMapToSPDPlanResponse)
                .collect(Collectors.toList());

        return new PageableObject<>(
                data,
                (Long) map.get("totalPages"),
                (Integer) map.get("currentPage"));
    }

    private Map<String, Object> convertSPDPlanResponseToMap(SPDPlanResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", response.getId());
        map.put("orderNumber", response.getOrderNumber());
        map.put("planName", response.getPlanName());
        map.put("projectId", response.getProjectId());
        map.put("projectName", response.getProjectName());
        map.put("level", response.getLevel());
        map.put("semesterName", response.getSemesterName());
        map.put("subjectName", response.getSubjectName());
        map.put("fromDate", response.getFromDate());
        map.put("toDate", response.getToDate());
        map.put("fromDateSemester", response.getFromDateSemester());
        map.put("toDateSemester", response.getToDateSemester());
        map.put("description", response.getDescription());
        map.put("status", response.getStatus());
        map.put("maxLateArrival", response.getMaxLateArrival());
        return map;
    }

    private SPDPlanResponse convertMapToSPDPlanResponse(Map<String, Object> map) {
        // Create a simple implementation that wraps the map
        return new SPDPlanResponse() {
            @Override
            public String getId() {
                return (String) map.get("id");
            }

            @Override
            public Long getOrderNumber() {
                Object value = map.get("orderNumber");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }

            @Override
            public String getPlanName() {
                return (String) map.get("planName");
            }

            @Override
            public String getProjectId() {
                return (String) map.get("projectId");
            }

            @Override
            public String getProjectName() {
                return (String) map.get("projectName");
            }

            @Override
            public String getLevel() {
                return (String) map.get("level");
            }

            @Override
            public String getSemesterName() {
                return (String) map.get("semesterName");
            }

            @Override
            public String getSubjectName() {
                return (String) map.get("subjectName");
            }

            @Override
            public Long getFromDate() {
                Object value = map.get("fromDate");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }

            @Override
            public Long getToDate() {
                Object value = map.get("toDate");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }

            @Override
            public Long getFromDateSemester() {
                Object value = map.get("fromDateSemester");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }

            @Override
            public Long getToDateSemester() {
                Object value = map.get("toDateSemester");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }

            @Override
            public String getDescription() {
                return (String) map.get("description");
            }

            @Override
            public Integer getStatus() {
                Object value = map.get("status");
                return value instanceof Number ? ((Number) value).intValue() : null;
            }

            @Override
            public Integer getMaxLateArrival() {
                Object value = map.get("maxLateArrival");
                return value instanceof Number ? ((Number) value).intValue() : null;
            }
        };
    }
}
