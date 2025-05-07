package udpm.hn.studentattendance.core.admin.subject_facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.ADSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.subject_facility.repository.Admin_SubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.subject_facility.service.Admin_SubjectFacilityService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Service
@RequiredArgsConstructor
public class Admin_SubjectFacilityServiceImpl implements Admin_SubjectFacilityService {

    private final Admin_SubjectFacilityRepository repository;

    private final SubjectRepository subjectRepository;

    private final FacilityRepository facilityRepository;

    public ResponseEntity<?> getListSubjectFacility(ADSubjectFacilitySearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return RouterHelper.responseSuccess("Lây danh sách bộ môn cơ sở thành công", PageableObject.of(repository.getAll(pageable, request)));
    }

    @Override
    public ResponseEntity<?> createSubjectFacility(ADSubjectFacilityCreateRequest request) {
        Facility facility = facilityRepository.findById(request.getFacilityId()).orElse(null);
        if (facility == null || facility.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null || subject.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (repository.isExistsSubjectFacility(facility.getId(), subject.getId(), null)) {
            return RouterHelper.responseError("Bộ môn đã tồn tại trong cơ sở này");
        }

        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);

        return RouterHelper.responseSuccess("Thêm bộ môn cơ sở thành công", repository.save(subjectFacility));
    }

    @Override
    public ResponseEntity<?> updateSubjectFacility(String id, ADSubjectFacilityUpdateRequest request) {
        SubjectFacility subjectFacility = repository.findById(id).orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId()).orElse(null);
        if (facility == null || facility.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null || subject.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (repository.isExistsSubjectFacility(facility.getId(), subject.getId(), subjectFacility.getId())) {
            return RouterHelper.responseError("Bộ môn đã tồn tại trong cơ sở này");
        }

        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);

        return RouterHelper.responseSuccess("Cập nhật bộ môn cơ sở thành công", repository.save(subjectFacility));
    }

    @Override
    public ResponseEntity<?> detailSubjectFacility(String id) {
        ADSubjectFacilityResponse subjectFacility = repository.getOneById(id).orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }
        return RouterHelper.responseSuccess("Lấy thông tin bộ môn cơ sở thành công", subjectFacility);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        SubjectFacility subjectFacility = repository.findById(id).orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }
        subjectFacility.setStatus(subjectFacility.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Thay đổi trạng thái thành công", repository.save(subjectFacility));
    }

}
