package udpm.hn.studentattendance.core.admin.levelproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.repository.ADLevelProjectRepository;
import udpm.hn.studentattendance.core.admin.levelproject.service.ADLevelProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.helpers.*;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonPlanDateRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class ADLevelProjectManagementServiceImpl implements ADLevelProjectManagementService {

    private final ADLevelProjectRepository repository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    private final CommonPlanDateRepository commonPlanDateRepository;

    public PageableObject getLevelProjects(ADLevelProjectSearchRequest request) {
        String key = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject.of(repository.getAll(PaginationHelper.createPageable(request, "id"), request)),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getListLevelProject(ADLevelProjectSearchRequest request) {
        PageableObject result = getLevelProjects(request);
        return RouterHelper.responseSuccess("Hiển thị tất cả nhóm dự án thành công", result);
    }

    @Override
    public ResponseEntity<?> createLevelProject(ADLevelProjectCreateRequest request) {


        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());

        if (repository.isExistsLevelProject(code, null)) {
            return RouterHelper.responseError("Nhóm dự án đã tồn tại trong hệ thống");
        }

        if (!ValidateHelper.isValidName(request.getName())) {
            return RouterHelper
                    .responseError("Tên không hợp lệ: Chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
        }

        LevelProject lv = new LevelProject();
        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject savedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("Vừa thêm nhóm dự án: " + savedLevel.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm nhóm dự án mới thành công", savedLevel);
    }

    @Override
    public ResponseEntity<?> updateLevelProject(String id, ADLevelProjectUpdateRequest request) {


        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Nhóm dự án không tồn tại");
        }

        if (!ValidateHelper.isValidName(request.getName())) {
            return RouterHelper
                    .responseError("Tên không hợp lệ: Chỉ được chứa ký tự chữ, số và các ký tự đặc biệt _ - #");
        }

        String code = CodeGeneratorUtils.generateCodeFromString(request.getName());
        if (repository.isExistsLevelProject(code, lv.getId())) {
            return RouterHelper.responseError("Mã dự án đã tồn tại vui lòng sửa mã dự án khác");
        }

        lv.setName(request.getName().trim());
        lv.setCode(code);
        lv.setDescription(request.getDescription());

        LevelProject updatedLevel = repository.save(lv);
        userActivityLogHelper.saveLog("Vừa cập nhật nhóm dự án " + updatedLevel.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật nhóm dự án thành công", updatedLevel);
    }

    public LevelProject getLevelProjectById(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ResponseEntity<?> detailLevelProject(String id) {
        LevelProject lv = getLevelProjectById(id);
        if (lv == null) {
            return RouterHelper.responseError("Nhóm dự án không tồn tại");
        }

        return RouterHelper.responseSuccess("Hiện thị chi tiết nhóm dự án thành công", lv);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        LevelProject lv = repository.findById(id).orElse(null);
        if (lv == null) {
            return RouterHelper.responseError("Nhóm dự án không tồn tại");
        }

        if (commonPlanDateRepository.existsNotYetStartedByLevelProject(lv.getId())) {
            return RouterHelper.responseError("Đang tồn tại ca chưa hoặc đang diễn ra. Không thể thay đổi trạng thái");
        }

        lv.setStatus(lv.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        LevelProject entity = repository.save(lv);

        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái nhóm dự án " + entity.getName() + " thành " + entity.getStatus().name());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái nhóm dự án thành công", entity);
    }

}
