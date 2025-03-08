package udpm.hn.studentattendance.core.admin.subject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.AdminSubjectRepository;
import udpm.hn.studentattendance.core.admin.subject.service.AdminSubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
public class AdminSubjectManagementServiceImpl implements AdminSubjectManagementService {

    @Autowired
    private AdminSubjectRepository adminSubjectRepository;

    @Override
    public ResponseObject<?> getListSubject(AdminSubjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(adminSubjectRepository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách bộ môn thành công"
        );
    }

    @Override
    public ResponseObject<?> createSubject(AdminSubjectCreateRequest request) {
        Subject s = new Subject();
        s = convertAdd(s, request);
        adminSubjectRepository.save(s);
        return new ResponseObject<>(s, HttpStatus.OK, "Thêm cấp bộ môn thành công");
    }

    @Override
    public ResponseObject<?> updateSubject(String id, AdminSubjectUpdateRequest request) {
        Subject s = adminSubjectRepository.findById(id).get();
        s = convertUpdate(s, request);
        adminSubjectRepository.save(s);
        return new ResponseObject<>(s, HttpStatus.OK, "Sửa bộ môn thành công");
    }

    @Override
    public ResponseObject<?> detailSubject(String id) {
        return adminSubjectRepository.findById(id)
                .map(s -> new ResponseObject<>(s, HttpStatus.OK, "Detail thành công!"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.CONFLICT, "Không tìm thấy bộ môn!"));
    }

    @Override
    public ResponseObject<?> deleteSubject(String id) {
        adminSubjectRepository.deleteById(id);
        return new ResponseObject<>(null, HttpStatus.OK, "Xóa bộ môn thành công");
    }

    private Subject convertAdd(Subject subject, AdminSubjectCreateRequest request) {
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setStatus(EntityStatus.ACTIVE);
        return subject;
    }

    private Subject convertUpdate(Subject subject, AdminSubjectUpdateRequest request) {
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        if (request.getStatus().equalsIgnoreCase("ACTIVE")) {
            subject.setStatus(EntityStatus.ACTIVE);
        } else {
            subject.setStatus(EntityStatus.INACTIVE);
        }
        return subject;
    }
}
