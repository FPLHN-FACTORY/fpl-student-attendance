package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.Admin_FacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.Admin_FacilityRepository;

import java.util.List;

@Service
public class Admin_FacilityManagementService {

    @Autowired
    private Admin_FacilityRepository repository;

    public List<Admin_FacilityResponse> getComboboxFacility() {
        return repository.getFacility();
    }

    public List<Admin_FacilityResponse> getComboboxFacilitySubject(Admin_SubjectFacilitySearchRequest request) {
        return repository.getListFacility(request);
    }
}
