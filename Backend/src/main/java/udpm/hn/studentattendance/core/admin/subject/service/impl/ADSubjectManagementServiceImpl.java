package udpm.hn.studentattendance.core.admin.subject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectRepository;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
@RequiredArgsConstructor
public class ADSubjectManagementServiceImpl implements ADSubjectManagementService {
    
    private final ADSubjectRepository adminSubjectRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    @Override
    public ResponseEntity<?> getListSubject(ADSubjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return RouterHelper.responseSuccess("Lây danh sách bộ môn thành công", PageableObject.of(adminSubjectRepository.getAll(pageable, request)));
    }

    @Override
    public ResponseEntity<?> createSubject(ADSubjectCreateRequest request) {
        if (!ValidateHelper.isValidCode(request.getCode())) {
            return RouterHelper.responseError("Mã bộ môn không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        Subject s = new Subject();
        s.setName(request.getName().trim());
        s.setCode(request.getCode().toUpperCase());

        if (adminSubjectRepository.isExistsCodeSubject(s.getCode(), null)) {
            return RouterHelper.responseError("Mã bộ môn đã tồn tại trên hệ thống");
        }

        if (adminSubjectRepository.isExistsNameSubject(s.getName(), null)) {
            return RouterHelper.responseError("Tên bộ môn đã tồn tại trên hệ thống");
        }

        return RouterHelper.responseSuccess("Thêm mới bộ môn thành công", adminSubjectRepository.save(s));
    }

    @Override
    public ResponseEntity<?> updateSubject(String id, ADSubjectUpdateRequest request) {

        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (!ValidateHelper.isValidCode(request.getCode())) {
            return RouterHelper.responseError("Mã bộ môn không hợp lệ:  không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        s.setName(request.getName().trim());
        s.setCode(request.getCode().toUpperCase());

        if (adminSubjectRepository.isExistsCodeSubject(s.getCode(), s.getId())) {
            return RouterHelper.responseError("Mã bộ môn đã tồn tại trên hệ thống");
        }

        if (adminSubjectRepository.isExistsNameSubject(s.getName(), s.getId())) {
            return RouterHelper.responseError("Tên bộ môn đã tồn tại trên hệ thống");
        }

        return RouterHelper.responseSuccess("Cập nhật bộ môn thành công", adminSubjectRepository.save(s));
    }

    @Override
    public ResponseEntity<?> detailSubject(String id) {
        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }
        return RouterHelper.responseSuccess("Lấy thông tin bộ môn thành công", s);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }
        s.setStatus(s.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        Subject newEntity = adminSubjectRepository.save(s);

        if (s.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdSubject(s.getId());
        }
        return RouterHelper.responseSuccess("Đổi trạng thái bộ môn thành công", newEntity);
    }

}
