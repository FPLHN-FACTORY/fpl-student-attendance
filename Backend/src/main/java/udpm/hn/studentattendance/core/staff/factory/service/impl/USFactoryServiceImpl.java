package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USDetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.*;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.*;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class USFactoryServiceImpl implements USFactoryService {

    private final USFactoryExtendRepository factoryRepository;

    private final USProjectFactoryExtendRepository projectFactoryExtendRepository;

    private final USStaffFactoryExtendRepository staffFactoryExtendRepository;

    private final USSubjectFacilityFactoryExtendRepository subjectFacilityFactoryExtendRepository;

    private final USFactoryPlanExtendRepository factoryPlanExtendRepository;

    private final USFactorySemesterExtendRepository semesterRepository;

    private final NotificationService notificationService;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    private final USFactoryProjectPlanExtendRepository projectPlanExtendRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    // private final USFactoryPlanDateExtendRepository planDateExtendRepository;
    //
    // private final USFactoryAttendanceExtendRepository attendanceExtendRepository;

    public PageableObject<USFactoryResponse> getCachedFactories(USFactoryRequest factoryRequest) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACTORY + "list_" +
                "facility=" + sessionHelper.getFacilityId() +
                "_name=" + (factoryRequest.getFactoryName() != null ? factoryRequest.getFactoryName() : "") +
                "_status=" + (factoryRequest.getStatus() != null ? factoryRequest.getStatus() : "null") +
                "_page=" + factoryRequest.getPage() +
                "_size=" + factoryRequest.getSize() +
                "_orderBy=" + factoryRequest.getOrderBy() +
                "_sortBy=" + factoryRequest.getSortBy() +
                "_q=" + (factoryRequest.getQ() != null ? factoryRequest.getQ() : "");

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(factoryRequest, "createdAt");
        PageableObject<USFactoryResponse> factories = PageableObject.of(
                factoryRepository.getAllFactory(pageable, sessionHelper.getFacilityId(),
                        factoryRequest));

        try {
            redisService.set(cacheKey, factories, redisTTL);
        } catch (Exception ignored) {
        }

        return factories;
    }

    @Override
    public ResponseEntity<?> getAllFactory(USFactoryRequest staffFactoryRequest) {
        PageableObject<USFactoryResponse> factories = getCachedFactories(staffFactoryRequest);
        return RouterHelper.responseSuccess("Hiển thị tất cả nhóm xưởng thành công", factories);
    }

    public List<USProjectFactoryResponse> getCachedProjects() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACTORY + "projects_" + sessionHelper.getFacilityId();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<USProjectFactoryResponse> projects = projectFactoryExtendRepository.getAllProject(
                sessionHelper.getFacilityId());

        try {
            redisService.set(cacheKey, projects, redisTTL);
        } catch (Exception ignored) {
        }

        return projects;
    }

    @Override
    public ResponseEntity<?> getAllProject() {
        List<USProjectFactoryResponse> projects = getCachedProjects();
        return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công", projects);
    }

    public List<SubjectFacility> getCachedSubjectFacilities() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SUBJECT_FACILITY + "list_" + sessionHelper.getFacilityId();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<SubjectFacility> subjectFacilities = subjectFacilityFactoryExtendRepository.getAllSubjectFacility(
                EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());

        try {
            redisService.set(cacheKey, subjectFacilities, redisTTL);
        } catch (Exception ignored) {
        }

        return subjectFacilities;
    }

    @Override
    public ResponseEntity<?> getAllSubjectFacility() {
        List<SubjectFacility> subjectFacilities = getCachedSubjectFacilities();
        return RouterHelper.responseSuccess("Lấy tất cả bộ môn cơ sở thành công", subjectFacilities);
    }

    public List<UserStaff> getCachedStaffs() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STAFF + "list_" +
                "facility=" + sessionHelper.getFacilityId() +
                "_role=" + RoleConstant.TEACHER;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<UserStaff> staffs = staffFactoryExtendRepository.getListUserStaff(EntityStatus.ACTIVE,
                EntityStatus.ACTIVE, sessionHelper.getFacilityId(), RoleConstant.TEACHER);

        try {
            redisService.set(cacheKey, staffs, redisTTL);
        } catch (Exception ignored) {
        }

        return staffs;
    }

    @Override
    public ResponseEntity<?> getAllStaff() {
        List<UserStaff> staffs = getCachedStaffs();
        return RouterHelper.responseSuccess("Lấy tất cả giảng viên theo cơ sở thành công", staffs);
    }

    public USDetailFactoryResponse getCachedFactoryById(String factoryId) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACTORY + factoryId;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, USDetailFactoryResponse.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Optional<USDetailFactoryResponse> factory = factoryRepository.getFactoryById(factoryId);

        USDetailFactoryResponse result = factory.orElse(null);
        if (result != null) {
            try {
                redisService.set(cacheKey, result, redisTTL);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    @Override
    public ResponseEntity<?> getDetailFactory(String factoryId) {
        USDetailFactoryResponse factory = getCachedFactoryById(factoryId);
        if (factory != null) {
            return RouterHelper.responseSuccess("Xem chi tiết nhóm xưởng thành công", factory);
        }
        return RouterHelper.responseError("Nhóm xưởng không tồn tại");
    }

    @Override
    public ResponseEntity<?> createFactory(USFactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        Optional<UserStaff> userStaff = staffFactoryExtendRepository
                .findById(factoryCreateUpdateRequest.getIdUserStaff());
        Optional<Project> project = projectFactoryExtendRepository
                .findById(factoryCreateUpdateRequest.getIdProject());

        if (userStaff.isEmpty()) {
            return RouterHelper.responseError("Giảng viên không tồn tại");
        }
        if (project.isEmpty()) {
            return RouterHelper.responseError("Dự án không tồn tại");
        }
        boolean exists = factoryRepository.isExistNameAndProject(factoryCreateUpdateRequest.getFactoryName(),
                factoryCreateUpdateRequest.getIdProject(), null);
        if (exists) {
            return RouterHelper.responseError("Nhóm xưởng đã tồn tại trong dự án này");
        }

        Factory factory = new Factory();
        factory.setName(factoryCreateUpdateRequest.getFactoryName());
        factory.setDescription(factoryCreateUpdateRequest.getFactoryDescription());
        factory.setUserStaff(userStaff.get());
        factory.setProject(project.get());
        factory.setStatus(EntityStatus.ACTIVE);

        Factory saveFactory = factoryRepository.save(factory);

        Map<String, Object> dataNotification = new HashMap<>();
        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
        dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        notificationAddRequest.setIdUser(userStaff.get().getId());
        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
        notificationAddRequest.setData(dataNotification);
        notificationService.add(notificationAddRequest);

        userActivityLogHelper
                .saveLog("vừa thêm 1 nhóm xưởng mới: " + saveFactory.getName() + " trong dự án "
                        + project.get().getName());

        // Invalidate factory caches
        invalidateFactoryCaches();

        return RouterHelper.responseSuccess("Thêm nhóm xưởng mới thành công", saveFactory);
    }

    @Override
    public ResponseEntity<?> updateFactory(USFactoryCreateUpdateRequest req) {
        Factory factory = factoryRepository.findById(req.getId())
                .orElseThrow();
        UserStaff newStaff = staffFactoryExtendRepository.findById(req.getIdUserStaff())
                .orElseThrow();
        Project newProject = projectFactoryExtendRepository.findById(req.getIdProject())
                .orElseThrow();

        if (factoryRepository.isExistNameAndProject(req.getFactoryName(), newProject.getId(), factory.getId())) {
            return RouterHelper.responseError("Nhóm xưởng đã tồn tại trong dự án này");
        }
        if (newStaff == null) {
            return RouterHelper.responseError("Giảng viên không tồn tại");
        }
        if (newProject == null) {
            return RouterHelper.responseError("Dự án không tồn tại");
        }
        if (factory == null) {
            return RouterHelper.responseError("Nhóm xưởng không tồn tại");
        }

        // 3. Nếu project thay đổi thì xử lý plan chỉ bằng 2 query:
        String oldProjectId = factory.getProject().getId();
        String newProjectId = newProject.getId();
        if (!oldProjectId.equals(newProjectId)) {
            // Lấy plan cũ & plan mới chỉ trong 2 lần gọi
            Plan oldPlan = projectPlanExtendRepository.getPlanByProjectId(oldProjectId);
            Plan newPlan = projectPlanExtendRepository.getPlanByProjectId(newProjectId);
            if (newPlan == null) {
                projectPlanExtendRepository.deleteAllAttendanceAndPlanDateAndPlanFactoryByPlan(oldPlan.getId());
            } else if (oldPlan != null && newPlan != null) {
                // Kiểm tra xem association đã tồn tại chưa, nếu chưa thì tạo mới
                boolean associationExists = factoryPlanExtendRepository
                        .existsByFactoryIdAndPlanId(factory.getId(), newPlan.getId());
                if (!associationExists) {
                    PlanFactory pf = factoryPlanExtendRepository.getPlanFactoryByFactoryId(factory.getId());
                    pf.setPlan(newPlan);
                    factoryPlanExtendRepository.save(pf);
                }
            }
        }

        String oldName = factory.getName();
        UserStaff oldStaff = factory.getUserStaff();
        factory.setName(req.getFactoryName());
        factory.setDescription(req.getFactoryDescription());
        factory.setUserStaff(newStaff);
        factory.setProject(newProject);
        Factory saveFactory = factoryRepository.save(factory);

        Map<String, Object> dataNotification = new HashMap<>();
        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
        dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
        notificationAddRequest.setData(dataNotification);
        if (!oldStaff.getId().equals(newStaff.getId())) {
            notificationAddRequest.setIdUser(oldStaff.getId());
            notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_TEACHER_TO_FACTORY);
            notificationService.add(notificationAddRequest);
        }
        notificationAddRequest.setIdUser(newStaff.getId());
        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
        notificationService.add(notificationAddRequest);

        userActivityLogHelper.saveLog("vừa cập nhật nhóm xưởng: " + oldName + " → " + saveFactory.getName());

        // Invalidate factory caches
        invalidateFactoryCache(factory.getId());

        return RouterHelper.responseSuccess("Cập nhật nhóm xưởng thành công", saveFactory);
    }

    @Override
    public ResponseEntity<?> changeStatus(String factoryId) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryId);
        if (existFactory.isPresent()) {
            Factory factory = existFactory.get();
            String oldStatus = factory.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
            factory.setStatus(existFactory.get().getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            String newStatus = factory.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
            Factory saveFactory = factoryRepository.save(factory);
            if (factory.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFactory(factory.getId());
            }
            userActivityLogHelper
                    .saveLog("vừa thay đổi trạng thái nhóm xưởng " + saveFactory.getName() + " từ " + oldStatus
                            + " thành " + newStatus);

            // Invalidate factory caches
            invalidateFactoryCache(factoryId);
        }
        return RouterHelper.responseSuccess("Đổi trạng thái nhóm xưởng thành công", null);
    }

    @Override
    public ResponseEntity<?> detailFactory(String factoryId) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryId);
        if (existFactory.isPresent()) {
            return RouterHelper.responseSuccess("Xem chi tiết nhóm xưởng thành công", existFactory);
        }
        return RouterHelper.responseError("Nhóm xưởng không tồn tại");
    }

    @Override
    public ResponseEntity<?> existsPlanByFactoryId(String factoryId) {
        boolean exists = factoryPlanExtendRepository.existsPlanByFactoryId(factoryId);
        return RouterHelper.responseSuccess("Lấy thông tin nhóm xưởng thành công", exists);
    }

    @Override
    public ResponseEntity<?> changeAllStatusByFactory() {
        Long now = new Date().getTime();
        String semesterId = null;

        for (Semester semester : semesterRepository.findAll()) {
            if (semester.getToDate() < now) {
                semesterId = semester.getId();
                break;
            }
        }

        if (semesterId == null) {
            return RouterHelper.responseError("Không có kỳ học nào đã kết thúc");
        }

        List<Factory> factories = factoryRepository.getAllFactoryBySemester(sessionHelper.getFacilityId(),
                semesterId);

        if (factories.isEmpty()) {
            return RouterHelper.responseSuccess("Không có nhóm xưởng nào thuộc kỳ học đã kết thúc",
                    factories);
        }

        for (Factory factory : factories) {
            factory.setStatus(factory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            factoryRepository.save(factory);
            if (factory.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFactory(factory.getId());
            }

            // Invalidate each factory's cache
            invalidateFactoryCache(factory.getId());
        }

        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái hàng loạt " + factories.size() + " nhóm xưởng của kỳ học đã kết thúc");

        // Invalidate all factory caches
        invalidateFactoryCaches();

        return RouterHelper.responseSuccess("Đổi trạng thái nhóm xưởng kỳ trước thành công", factories);
    }

    public List<Semester> getCachedSemesters() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "semesters";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<Semester> semesters = semesterRepository.findAll();

        try {
            redisService.set(cacheKey, semesters, redisTTL);
        } catch (Exception ignored) {
        }

        return semesters;
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = getCachedSemesters();
        return RouterHelper.responseSuccess("Lấy thành công tất cả học kỳ", semesters);
    }

    /**
     * Xóa toàn bộ cache liên quan đến nhóm xưởng
     */
    private void invalidateFactoryCaches() {
        redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACTORY + "list_*");
        redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_FACTORY + "projects_*");
    }

    /**
     * Xóa cache của nhóm xưởng cụ thể và danh sách nhóm xưởng
     */
    private void invalidateFactoryCache(String factoryId) {
        redisService.delete(RedisPrefixConstant.REDIS_PREFIX_FACTORY + factoryId);
        invalidateFactoryCaches();
    }
}
