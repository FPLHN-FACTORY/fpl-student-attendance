package udpm.hn.studentattendance.core.admin.userstaff.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;

public interface ADStaffService {
    ResponseEntity<?> getAllStaffByFilter(ADStaffRequest adStaffRequest);

    ResponseEntity<?> createStaff(@Valid ADCreateUpdateStaffRequest adCreateUpdateStaffRequest);

    ResponseEntity<?> updateStaff(@Valid ADCreateUpdateStaffRequest adCreateUpdateStaffRequest, String id);

    ResponseEntity<?> changeStaffStatus(String staffId);

    ResponseEntity<?> getStaffById(String staffId);

    ResponseEntity<?> getAllRole();

    ResponseEntity<?> getAllFacility();

}
