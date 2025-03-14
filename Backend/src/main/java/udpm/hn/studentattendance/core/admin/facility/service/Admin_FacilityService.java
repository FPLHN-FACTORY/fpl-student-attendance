package udpm.hn.studentattendance.core.admin.facility.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_CreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_FacilitySearchRequest;


public interface Admin_FacilityService {
    ResponseEntity<?> getAllFacility(Admin_FacilitySearchRequest request);

    ResponseEntity<?> createFacility(@Valid Admin_CreateUpdateFacilityRequest request);

    ResponseEntity<?> updateFacility(String facilityId, @Valid Admin_CreateUpdateFacilityRequest request);

    ResponseEntity<?> changeFacilityStatus(String facilityId);

    ResponseEntity<?> getFacilityById(String facilityId);
}
