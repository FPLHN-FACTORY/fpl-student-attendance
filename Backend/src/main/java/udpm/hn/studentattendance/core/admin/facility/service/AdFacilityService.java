package udpm.hn.studentattendance.core.admin.facility.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.CreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.FacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.FacilitySearchRequest;


public interface AdFacilityService {
    ResponseEntity<?> getAllFacility(FacilitySearchRequest request);

    ResponseEntity<?> createFacility(@Valid CreateUpdateFacilityRequest request);

    ResponseEntity<?> updateFacility(String facilityId, @Valid CreateUpdateFacilityRequest request);

    ResponseEntity<?> changeFacilityStatus(String facilityId);

    ResponseEntity<?> getFacilityById(String facilityId);
}
