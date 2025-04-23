package udpm.hn.studentattendance.core.admin.facility.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;


public interface AFFacilityService {
    ResponseEntity<?> getAllFacility(AFFacilitySearchRequest request);

    ResponseEntity<?> createFacility(AFCreateUpdateFacilityRequest request);

    ResponseEntity<?> updateFacility(String facilityId, AFCreateUpdateFacilityRequest request);

    ResponseEntity<?> changeFacilityStatus(String facilityId);

    ResponseEntity<?> getFacilityById(String facilityId);

    ResponseEntity<?> up(String facilityId);

    ResponseEntity<?> down(String facilityId);

}
