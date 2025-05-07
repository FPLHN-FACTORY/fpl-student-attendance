package udpm.hn.studentattendance.core.admin.subject_facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.core.admin.subject_facility.repository.Admin_FacilityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Admin_FacilityManagementService {

    private final Admin_FacilityRepository repository;

    public List<ADFacilityResponse> getComboboxFacility() {
        return repository.getFacility();
    }

    public List<ADFacilityResponse> getComboboxFacilitySubject(ADSubjectFacilitySearchRequest request) {
        return repository.getListFacility(request);
    }
}
