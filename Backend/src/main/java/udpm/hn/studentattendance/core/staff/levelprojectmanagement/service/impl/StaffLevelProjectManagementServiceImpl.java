package udpm.hn.studentattendance.core.staff.levelprojectmanagement.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.StaffLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.repository.StaffLevelProjectRepository;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.service.StaffLevelProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
public class StaffLevelProjectManagementServiceImpl implements StaffLevelProjectManagementService {

    @Autowired
    private StaffLevelProjectRepository repository;

    @Override
    public ResponseObject<?> getListLevelProject(StaffLevelProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách cấp dự án thành công"
        );
    }

    @Override
    public ResponseObject<?> createLevelProject(LevelProjectCreateRequest request) {
        LevelProject lv = new LevelProject();
        lv = convertAdd(request, lv);
        repository.save(lv);
        return new ResponseObject<>(lv, HttpStatus.OK, "Thêm cấp dự án thành công");
    }

    @Override
    public ResponseObject<?> updateLevelProject(String id, LevelProjectUpdateRequest request) {
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
    public ResponseObject<?> deleteLevelProject(String id) {
        repository.deleteById(id);
        return new ResponseObject<>(null, HttpStatus.OK, "Xóa cấp dự án thành công");
    }

    private LevelProject convertAdd(LevelProjectCreateRequest request, LevelProject lv) {
        lv.setName(request.getName());
        lv.setCode(request.getCode());
        lv.setDescription(request.getDescription());
        lv.setStatus(EntityStatus.ACTIVE);
        return lv;
    }

    private LevelProject convertUpdate(LevelProjectUpdateRequest request, LevelProject lv) {
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
