package udpm.hn.studentattendance.core.admin.user_staff.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.Admin_CreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.Admin_StaffRequest;

public interface Admin_StaffService {
    ResponseEntity<?> getAllStaffByFilter(Admin_StaffRequest adStaffRequest);

    ResponseEntity<?> createStaff(@Valid Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest);

    ResponseEntity<?> updateStaff(@Valid Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest, String id);

    ResponseEntity<?> changeStaffStatus(String staffId);

    ResponseEntity<?> getStaffById(String staffId);

    ResponseEntity<?> getAllRole();

    ResponseEntity<?> getAllFacility();

}
