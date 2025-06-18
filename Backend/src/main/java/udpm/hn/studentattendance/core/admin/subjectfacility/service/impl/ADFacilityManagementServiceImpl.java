package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADFacilityManagementService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
@RequiredArgsConstructor
public class ADFacilityManagementServiceImpl implements ADFacilityManagementService {

    private final ADFacilityRepository repository;


    @Override
    public ResponseEntity<?> getComboboxFacility(String idSubject) {
        return RouterHelper.responseSuccess("Lấy danh sách cơ sở thành công", repository.getFacility(idSubject));
    }

    @Override
    public ResponseEntity<?> getListFacility() {
        return RouterHelper.responseSuccess("Lấy danh sách cơ sở để lọc thành công", repository.getFacilities(EntityStatus.ACTIVE));
    }
}
