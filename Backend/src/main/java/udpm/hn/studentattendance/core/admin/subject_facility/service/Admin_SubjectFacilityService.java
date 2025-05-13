package udpm.hn.studentattendance.core.admin.subject_facility.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Admin_SubjectFacilityService {

    ResponseEntity<?> getListSubjectFacility(ADSubjectFacilitySearchRequest request);

    ResponseEntity<?> createSubjectFacility(ADSubjectFacilityCreateRequest request);

    ResponseEntity<?> updateSubjectFacility(String id, ADSubjectFacilityUpdateRequest request);

    ResponseEntity<?> detailSubjectFacility(String id);

    ResponseEntity<?> changeStatus(String id);

}
