package udpm.hn.studentattendance.core.admin.staff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_ChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.service.Admin_StaffRoleService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_STAFF_ROLE_MANAGEMENT)
public class Admin_StaffRoleRestController {

    private final Admin_StaffRoleService adStaffRoleService;

    @GetMapping("/{staffId}")
    public ResponseEntity<?> getAllRole(@PathVariable String staffId) {
        return adStaffRoleService.getAllRole(staffId);
    }

    @GetMapping("/role-check")
    public ResponseEntity<?> getRoleCheck(Admin_StaffRoleRequest adStaffRoleRequest) {
        return adStaffRoleService.getRoleChecked(adStaffRoleRequest);
    }

    @PutMapping("/change-role")
    public ResponseEntity<?> changeStaffRole(@RequestBody Admin_ChangeStaffRoleRequest adChangeStaffRoleRequest) {
        return adStaffRoleService.changeStaffRole(adChangeStaffRoleRequest);
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getFacilities() {
        return adStaffRoleService.getFacilities();
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable("roleId") String roleId) {
        return adStaffRoleService.deleteStaffRole(roleId);
    }


}
