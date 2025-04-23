package udpm.hn.studentattendance.core.admin.level_project.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.level_project.repository.Admin_LevelProjectRepository;
import udpm.hn.studentattendance.core.admin.level_project.service.Admin_LevelProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
public class Admin_LevelProjectManagementServiceImpl implements Admin_LevelProjectManagementService {

    @Autowired
    private Admin_LevelProjectRepository repository;

    @Override
    public ResponseObject<?> getListLevelProject(Admin_LevelProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách cấp dự án thành công"
        );
    }

    @Override
    public ResponseObject<?> createLevelProject(Admin_LevelProjectCreateRequest request) {
        LevelProject lv = new LevelProject();
        lv = convertAdd(request, lv);
        repository.save(lv);
        return new ResponseObject<>(lv, HttpStatus.OK, "Thêm cấp dự án thành công");
    }

    @Override
    public ResponseObject<?> updateLevelProject(String id, Admin_LevelProjectUpdateRequest request) {
        LevelProject lv = repository.findById(id).get();
        lv = convertUpdate(request, lv);
        repository.save(lv);
        return new ResponseObject<>(lv, HttpStatus.OK, "Sửa cấp dự án thành công");
    }

    @Override
    public ResponseObject<?> detailLevelProject(String id) {
        return repository.findById(id)
                .map(lv -> new ResponseObject<>(lv, HttpStatus.OK, "Detail thành công!"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.CONFLICT, "Không tìm thấy cấp dự án!"));
    }

    @Override
    public ResponseObject<?> changeStatus(String id) {
        LevelProject lv = repository.findById(id).get();
        if (lv.getStatus() == EntityStatus.ACTIVE) {
            lv.setStatus(EntityStatus.INACTIVE);
        } else {
            lv.setStatus(EntityStatus.ACTIVE);
        }
        repository.save(lv);
        return new ResponseObject<>(lv, HttpStatus.OK, "Chuyển trạng thái cấp dự án thành công");
    }

    private LevelProject convertAdd(Admin_LevelProjectCreateRequest request, LevelProject lv) {
        lv.setName(request.getName());
        lv.setCode(request.getCode());
        lv.setDescription(request.getDescription());
        lv.setStatus(EntityStatus.ACTIVE);
        return lv;
    }

    private LevelProject convertUpdate(Admin_LevelProjectUpdateRequest request, LevelProject lv) {
        lv.setName(request.getName());
        lv.setCode(request.getCode());
        lv.setDescription(request.getDescription());
        if (request.getStatus().equalsIgnoreCase("ACTIVE")) {
            lv.setStatus(EntityStatus.ACTIVE);
        } else {
            lv.setStatus(EntityStatus.INACTIVE);
        }
        return lv;
    }
}
