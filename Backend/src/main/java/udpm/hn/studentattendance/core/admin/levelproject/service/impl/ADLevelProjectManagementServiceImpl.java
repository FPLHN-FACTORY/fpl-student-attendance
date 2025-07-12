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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
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

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public PageableObject getLevelProjects(ADLevelProjectSearchRequest request) {
        // Tạo cache key sử dụng phương thức toString
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "list_" + request.toString();

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
            redisService.setObject(cacheKey, result);
        } catch (Exception ignored) {
        }

        return result;
    }

    @Override
    public ResponseEntity<?> getListLevelProject(ADLevelProjectSearchRequest request) {
        PageableObject result = getLevelProjects(request);
        return RouterHelper.responseSuccess("Get level project list successfully", result);
    }

    @Override
    public ResponseEntity<?> createLevelProject(ADLevelProjectCreateRequest request) {
        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());

        if (repository.isExistsLevelProject(code, null)) {
            return RouterHelper.responseError("Level project already exists in the system");
        }

        LevelProject lv = new LevelProject();
        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject savedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("just added level project " + savedLevel.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Add new level project successfully", savedLevel);
    }

    @Override
    public ResponseEntity<?> updateLevelProject(String id, ADLevelProjectUpdateRequest request) {
        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Level project not found for editing");
        }

        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());
        if (repository.isExistsLevelProject(code, lv.getId())) {
            return RouterHelper.responseError("Level project already exists in the system");
        }

        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject updatedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("just updated level project " + updatedLevel.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Update level project successfully", updatedLevel);
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
                redisService.setObject(cacheKey, lv);
            } catch (Exception ignored) {
            }
        }

        return lv;
    }

    @Override
    public ResponseEntity<?> detailLevelProject(String id) {
        LevelProject lv = getLevelProjectById(id);
        if (lv == null) {
            return RouterHelper.responseError("Level project not found");
        }

        return RouterHelper.responseSuccess("Get level project details successfully", lv);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Level project not found for status change");
        }
        lv.setStatus(lv.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        LevelProject entity = repository.save(lv);
        if (entity.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdLevelProject(entity.getId());
        }
        userActivityLogHelper.saveLog(
                "just changed level project status " + entity.getName() + " to " + entity.getStatus().name());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Change level project status successfully", entity);
    }

    /**
     * @deprecated Use redisInvalidationHelper.invalidateAllCaches() instead
     */
    private void invalidateLevelProjectCaches() {
        redisInvalidationHelper.invalidateAllCaches();
    }

    /**
     * @deprecated Use redisInvalidationHelper.invalidateAllCaches() instead
     */
    private void invalidateLevelProjectCache(String id) {
        redisInvalidationHelper.invalidateAllCaches();
    }
}
