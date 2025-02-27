package udpm.hn.studentattendance.core.admin.role.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.role.model.request.AdCreateUpdateRoleRequest;
import udpm.hn.studentattendance.core.admin.role.model.request.AdRoleRequest;

public interface AdRoleService {
    ResponseEntity<?> getAllRole(AdRoleRequest adRoleRequest);

    ResponseEntity<?> getDetailRole(String roleId);

    ResponseEntity<?> getFacilities();

    ResponseEntity<?> deleteRole(String roleId);


}
