package udpm.hn.studentattendance.core.admin.subject_facility.service;


import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;

public interface ADFacilityManagementService {

    ResponseEntity<?> getComboboxFacility();

    ResponseEntity<?> getComboboxFacilitySubject(ADSubjectFacilitySearchRequest request);

}
