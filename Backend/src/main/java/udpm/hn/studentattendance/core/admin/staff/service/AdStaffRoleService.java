package udpm.hn.studentattendance.core.admin.staff.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;

public interface AdStaffRoleService {
    ResponseEntity<?> getAllRole(String staffId);

    ResponseEntity<?> getRoleChecked(AdStaffRoleRequest adStaffRoleRequest);

    ResponseEntity<?> changeStaffRole(AdChangeStaffRoleRequest adChangeStaffRoleRequest);

    ResponseEntity<?> getFacilities();

    ResponseEntity<?> getFacilitiesSelect(String idStaff);

    ResponseEntity<?> getAllFactoryByFacility(String facilityId);
}
