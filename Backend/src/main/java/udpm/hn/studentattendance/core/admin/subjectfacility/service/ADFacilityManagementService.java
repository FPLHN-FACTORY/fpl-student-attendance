package udpm.hn.studentattendance.core.admin.subjectfacility.service;


import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;

public interface ADFacilityManagementService {

    ResponseEntity<?> getComboboxFacility(String idSubject);

    ResponseEntity<?> getListFacility();


}
