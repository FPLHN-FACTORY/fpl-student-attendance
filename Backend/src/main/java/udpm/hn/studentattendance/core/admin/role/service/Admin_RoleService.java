package udpm.hn.studentattendance.core.admin.role.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.role.model.request.Admin_RoleRequest;

public interface Admin_RoleService {
    ResponseEntity<?> getAllRole(Admin_RoleRequest adRoleRequest);

    ResponseEntity<?> getDetailRole(String roleId);

    ResponseEntity<?> getFacilities();

    ResponseEntity<?> deleteRole(String roleId);


}
