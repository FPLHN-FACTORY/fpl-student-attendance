package udpm.hn.studentattendance.core.admin.subject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectRepository;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
public class ADSubjectManagementServiceImpl implements ADSubjectManagementService {

    @Autowired
    private ADSubjectRepository adminSubjectRepository;

    @Override
    public ResponseObject<?> getListSubject(ADSubjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(adminSubjectRepository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách bộ môn thành công");
    }

    @Override
    public ResponseObject<?> createSubject(ADSubjectCreateRequest request) {
        Subject s = new Subject();
        s = convertAdd(s, request);
        adminSubjectRepository.save(s);
        return new ResponseObject<>(s, HttpStatus.OK, "Thêm cấp bộ môn thành công");
    }

    @Override
    public ResponseObject<?> updateSubject(String id, ADSubjectUpdateRequest request) {
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

    private Subject convertAdd(Subject subject, ADSubjectCreateRequest request) {
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setStatus(EntityStatus.ACTIVE);
        return subject;
    }

    private Subject convertUpdate(Subject subject, ADSubjectUpdateRequest request) {
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
