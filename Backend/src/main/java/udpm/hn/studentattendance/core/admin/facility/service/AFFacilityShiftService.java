package udpm.hn.studentattendance.core.admin.facility.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;


public interface AFFacilityShiftService {

    ResponseEntity<?> getAllList(AFFilterFacilityShiftRequest request);

    ResponseEntity<?> addShift(AFAddOrUpdateFacilityShiftRequest request);

    ResponseEntity<?> updateShift(AFAddOrUpdateFacilityShiftRequest request);

    ResponseEntity<?> deleteShift(String id);

    ResponseEntity<?> changeStatus(String id);

}
