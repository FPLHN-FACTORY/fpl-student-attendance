package udpm.hn.studentattendance.core.admin.subject_facility.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.core.admin.subject_facility.repository.Admin_FacilityRepository;

import java.util.List;

@Service
public class Admin_FacilityManagementService {

    @Autowired
    private Admin_FacilityRepository repository;

    public List<ADFacilityResponse> getComboboxFacility() {
        return repository.getFacility();
    }

    public List<ADFacilityResponse> getComboboxFacilitySubject(ADSubjectFacilitySearchRequest request) {
        return repository.getListFacility(request);
    }
}
