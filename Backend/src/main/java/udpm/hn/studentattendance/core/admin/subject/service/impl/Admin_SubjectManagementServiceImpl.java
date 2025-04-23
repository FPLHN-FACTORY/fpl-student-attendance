package udpm.hn.studentattendance.core.admin.subject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.Admin_SubjectRepository;
import udpm.hn.studentattendance.core.admin.subject.service.Admin_SubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
public class Admin_SubjectManagementServiceImpl implements Admin_SubjectManagementService {

    @Autowired
    private Admin_SubjectRepository adminSubjectRepository;

    @Override
    public ResponseObject<?> getListSubject(Admin_SubjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(adminSubjectRepository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách bộ môn thành công");
    }

    @Override
    public ResponseObject<?> createSubject(Admin_SubjectCreateRequest request) {
        Subject s = new Subject();
        s = convertAdd(s, request);
        adminSubjectRepository.save(s);
        return new ResponseObject<>(s, HttpStatus.OK, "Thêm cấp bộ môn thành công");
    }

    @Override
    public ResponseObject<?> updateSubject(String id, Admin_SubjectUpdateRequest request) {
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
    public ResponseObject<?> changeStatus(String id) {
        Subject s = adminSubjectRepository.findById(id).get();
        if (s.getStatus() == EntityStatus.ACTIVE) {
            s.setStatus(EntityStatus.INACTIVE);
        } else {
            s.setStatus(EntityStatus.ACTIVE);
        }
        adminSubjectRepository.save(s);
        return new ResponseObject<>(null, HttpStatus.OK, "Đổi trạng thái bộ môn thành công");
    }

    private Subject convertAdd(Subject subject, Admin_SubjectCreateRequest request) {
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setStatus(EntityStatus.ACTIVE);
        return subject;
    }

    private Subject convertUpdate(Subject subject, Admin_SubjectUpdateRequest request) {
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
