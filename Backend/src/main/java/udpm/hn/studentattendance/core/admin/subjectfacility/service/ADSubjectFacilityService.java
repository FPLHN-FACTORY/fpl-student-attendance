package udpm.hn.studentattendance.core.admin.subjectfacility.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;

public interface ADSubjectFacilityService {

    ResponseEntity<?> getListSubjectFacility(ADSubjectFacilitySearchRequest request);

    ResponseEntity<?> createSubjectFacility(ADSubjectFacilityCreateRequest request);

    ResponseEntity<?> updateSubjectFacility(String id, ADSubjectFacilityUpdateRequest request);

    ResponseEntity<?> detailSubjectFacility(String id);

    ResponseEntity<?> changeStatus(String id);

}
