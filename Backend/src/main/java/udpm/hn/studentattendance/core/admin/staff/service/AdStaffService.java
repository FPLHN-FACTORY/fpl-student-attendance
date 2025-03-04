package udpm.hn.studentattendance.core.admin.staff.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRequest;

public interface AdStaffService {
    ResponseEntity<?> getAllStaffByFilter(AdStaffRequest adStaffRequest);

    ResponseEntity<?> createStaff(@Valid AdCreateUpdateStaffRequest adCreateUpdateStaffRequest);

    ResponseEntity<?> updateStaff(@Valid AdCreateUpdateStaffRequest adCreateUpdateStaffRequest);

    ResponseEntity<?> changeStaffStatus(String staffId);

    ResponseEntity<?> getStaffById(String staffId);
}
