package udpm.hn.studentattendance.core.admin.facility.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;

public interface AFFacilityIPService {

    ResponseEntity<?> getAllList(AFFilterFacilityIPRequest request);

    ResponseEntity<?> addIP(AFAddOrUpdateFacilityIPRequest request);

    ResponseEntity<?> updateIP(AFAddOrUpdateFacilityIPRequest request);

    ResponseEntity<?> deleteIP(String id);

    ResponseEntity<?> changeStatus(String id);

}
