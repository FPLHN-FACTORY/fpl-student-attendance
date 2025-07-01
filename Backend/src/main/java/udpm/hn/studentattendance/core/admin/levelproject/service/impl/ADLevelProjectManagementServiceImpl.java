package udpm.hn.studentattendance.core.admin.levelproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.repository.ADLevelProjectRepository;
import udpm.hn.studentattendance.core.admin.levelproject.service.ADLevelProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;

@Service
@RequiredArgsConstructor
public class ADLevelProjectManagementServiceImpl implements ADLevelProjectManagementService {

    private final ADLevelProjectRepository repository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;
    private final RedisService redisService;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public PageableObject getLevelProjects(ADLevelProjectSearchRequest request) {
        // Tạo cache key thủ công
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                "_name=" + (request.getName() != null ? request.getName() : "") +
                "_status=" + (request.getStatus() != null ? request.getStatus() : "");

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(request, "id");
        PageableObject result = PageableObject.of(repository.getAll(pageable, request));

        try {
            redisService.set(cacheKey, result, redisTTL);
        } catch (Exception ignored) {
        }

        return result;
    }

    @Override
    public ResponseEntity<?> getListLevelProject(ADLevelProjectSearchRequest request) {
        PageableObject result = getLevelProjects(request);
        return RouterHelper.responseSuccess("Lấy danh sách cấp độ dự án thành công", result);
    }

    @Override
    public ResponseEntity<?> createLevelProject(ADLevelProjectCreateRequest request) {
        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());

        if (repository.isExistsLevelProject(code, null)) {
            return RouterHelper.responseError("Cấp độ dự án đã tồn tại trên hệ thống");
        }

        LevelProject lv = new LevelProject();
        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject savedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("vừa thêm cấp độ dự án " + savedLevel.getName());

        invalidateLevelProjectCaches();

        return RouterHelper.responseSuccess("Thêm mới cấp độ dự án thành công", savedLevel);
    }

    @Override
    public ResponseEntity<?> updateLevelProject(String id, ADLevelProjectUpdateRequest request) {
        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Không tìm thây cấp độ dự án muốn chỉnh sửa");
        }

        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());
        if (repository.isExistsLevelProject(code, lv.getId())) {
            return RouterHelper.responseError("Cấp độ dự án đã tồn tại trên hệ thống");
        }

        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject updatedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("vừa cập nhật cấp độ dự án " + updatedLevel.getName());

        // Invalidate specific cache for this level project
        invalidateLevelProjectCache(id);

        return RouterHelper.responseSuccess("Cập nhật cấp độ dự án thành công", updatedLevel);
    }

    // Phương thức helper để lấy thông tin chi tiết cấp độ dự án từ cache hoặc DB
    public LevelProject getLevelProjectById(String id) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + id;

        // Kiểm tra cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, LevelProject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        // Cache miss - fetch from database
        LevelProject lv = repository.findById(id).orElse(null);

        if (lv != null) {
            // Store in cache
            try {
                redisService.set(cacheKey, lv, redisTTL);
            } catch (Exception ignored) {
            }
        }

        return lv;
    }

    @Override
    public ResponseEntity<?> detailLevelProject(String id) {
        LevelProject lv = getLevelProjectById(id);
        if (lv == null) {
            return RouterHelper.responseError("Không tìm thây cấp độ dự án");
        }

        return RouterHelper.responseSuccess("Lấy thông tin cấp độ dự án thành công", lv);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Không tìm thây cấp độ dự án muốn thay đổi trạng thái");
        }
        lv.setStatus(lv.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        LevelProject entity = repository.save(lv);
        if (entity.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdLevelProject(entity.getId());
        }
        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái cấp độ dự án " + entity.getName() + " thành " + entity.getStatus().name());

        // Invalidate specific cache for this level project
        invalidateLevelProjectCache(id);

        return RouterHelper.responseSuccess("Chuyển trạng thái cấp độ dự án thành công", entity);
    }

    /**
     * Helper method to invalidate all level project-related caches
     */
    private void invalidateLevelProjectCaches() {
        // Invalidate all level project lists
        redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_LEVEL + "list_*");
        // Invalidate plan level caches that might use this data
        redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_PLAN + "levels_*");
    }

    /**
     * Helper method to invalidate cache for specific level project and related
     * caches
     */
    private void invalidateLevelProjectCache(String id) {
        redisService.delete(RedisPrefixConstant.REDIS_PREFIX_LEVEL + id);
        invalidateLevelProjectCaches();
    }
}
