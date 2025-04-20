package udpm.hn.studentattendance.core.admin.staff.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_ChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRoleRequest;

public interface Admin_StaffRoleService {
    ResponseEntity<?> getAllRole(String staffId);

    ResponseEntity<?> getRoleChecked(Admin_StaffRoleRequest adStaffRoleRequest);

    ResponseEntity<?> changeStaffRole(Admin_ChangeStaffRoleRequest adChangeStaffRoleRequest);

    ResponseEntity<?> getFacilities();

    ResponseEntity<?> deleteStaffRole(String roleId);

}
