package udpm.hn.studentattendance.core.admin.levelproject.service.impl;

import lombok.RequiredArgsConstructor;
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
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;

@Service
@RequiredArgsConstructor
public class ADLevelProjectManagementServiceImpl implements ADLevelProjectManagementService {

    private final ADLevelProjectRepository repository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    @Override
    public ResponseEntity<?> getListLevelProject(ADLevelProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return RouterHelper.responseSuccess("Lấy danh sách cấp độ dự án thành công",
                PageableObject.of(repository.getAll(pageable, request)));
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
        return RouterHelper.responseSuccess("Cập nhật cấp độ dự án thành công", updatedLevel);
    }

    @Override
    public ResponseEntity<?> detailLevelProject(String id) {
        LevelProject lv = repository.findById(id).orElse(null);
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
        return RouterHelper.responseSuccess("Chuyển trạng thái cấp độ dự án thành công", entity);
    }

}
