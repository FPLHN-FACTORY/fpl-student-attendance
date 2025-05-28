package udpm.hn.studentattendance.core.admin.subject_facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.core.admin.subject_facility.service.ADFacilityManagementService;
import udpm.hn.studentattendance.helpers.RouterHelper;

@Service
@RequiredArgsConstructor
public class ADFacilityManagementServiceImpl implements ADFacilityManagementService {

    private final ADFacilityRepository repository;

    public ResponseEntity<?> getComboboxFacility() {
        return RouterHelper.responseSuccess("Lấy danh sách cơ sở thành công", repository.getFacility());
    }

    public ResponseEntity<?> getComboboxFacilitySubject(ADSubjectFacilitySearchRequest request) {
        return RouterHelper.responseSuccess("Lấy danh sách bộ môn theo cơ sở thành công", repository.getListFacility(request));
    }

}
