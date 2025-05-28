package udpm.hn.studentattendance.core.admin.facility.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;

public interface AFFacilityLocationService {

    ResponseEntity<?> getAllList(AFFilterFacilityLocationRequest request);

    ResponseEntity<?> addLocation(AFAddOrUpdateFacilityLocationRequest request);

    ResponseEntity<?> updateLocation(AFAddOrUpdateFacilityLocationRequest request);

    ResponseEntity<?> deleteLocation(String id);

    ResponseEntity<?> changeStatus(String id);

}
