package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.AdminSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.AdminFacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.AdminFacilityRepository;

import java.util.List;

@Service
public class AdminFacilityManagementService {

    @Autowired
    private AdminFacilityRepository repository;

    public List<AdminFacilityResponse> getComboboxFacility() {
        return repository.getFacility();
    }

    public List<AdminFacilityResponse> getComboboxFacilitySubject(AdminSubjectFacilitySearchRequest request) {
        return repository.getListFacility(request);
    }
}
