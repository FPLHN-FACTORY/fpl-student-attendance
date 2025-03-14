package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.Admin_SubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.Admin_SubjectFacilityService;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Service
public class Admin_SubjectFacilityServiceImpl implements Admin_SubjectFacilityService {

    @Autowired
    private Admin_SubjectFacilityRepository repository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Override
    public ResponseObject<?> getListSubjectFacility(Admin_SubjectFacilitySearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getAll(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách bộ môn cơ sở thành công"
        );
    }

    @Override
    public ResponseObject<?> createSubjectFacility(Admin_SubjectFacilityCreateRequest request) {
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility = convertAdd(subjectFacility, request);
        repository.save(subjectFacility);
        return new ResponseObject<>(subjectFacility , HttpStatus.OK, "Thêm bộ môn cơ ở");
    }

    @Override
    public ResponseObject<?> updateSubjectFacility(String id, Admin_SubjectFacilityUpdateRequest request) {
        SubjectFacility subjectFacility = repository.findById(id).get();
        subjectFacility = convertUpdate(subjectFacility, request);
        repository.save(subjectFacility);
        return new ResponseObject<>(subjectFacility , HttpStatus.OK, "sửa bộ môn cơ ở");
    }

    @Override
    public ResponseObject<?> detailSubjectFacility(String id) {
        return repository.findById(id)
                .map(s -> new ResponseObject<>(s, HttpStatus.OK, "Detail thành công!"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.CONFLICT, "Không tìm thấy ọo môn cơ sở!"));
    }

    @Override
    public ResponseObject<?> deleteSubjectFacility(String id) {
        repository.deleteById(id);
        return new ResponseObject<>(null, HttpStatus.OK, "Xóa dự án thành công");
    }

    private SubjectFacility convertAdd(SubjectFacility s, Admin_SubjectFacilityCreateRequest request) {
        s.setFacility(facilityRepository.findById(request.getFacilityId()).get());
        s.setSubject(subjectRepository.findById(request.getSubjectId()).get());
        return s;
    }
    private SubjectFacility convertUpdate(SubjectFacility s, Admin_SubjectFacilityUpdateRequest request) {
        s.setFacility(facilityRepository.findById(request.getFacilityId()).get());
        s.setSubject(subjectRepository.findById(request.getSubjectId()).get());
        if (request.getStatus().equals("ACTIVE")) {
            s.setStatus(EntityStatus.ACTIVE);
        } else {
            s.setStatus(EntityStatus.INACTIVE);
        }
        return s;
    }
}
