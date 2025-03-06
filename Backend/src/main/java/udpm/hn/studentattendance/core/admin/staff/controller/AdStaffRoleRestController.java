package udpm.hn.studentattendance.core.admin.staff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.service.AdStaffRoleService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RoutesConstant.URL_ADMIN_STAFF_ROLE_MANAGEMENT)
public class AdStaffRoleRestController {

    private final AdStaffRoleService adStaffRoleService;

    @GetMapping("/{staffId}")
    public ResponseEntity<?> getAllRole(@PathVariable String staffId) {
        return adStaffRoleService.getAllRole(staffId);
    }

    @GetMapping("/role-check")
    public ResponseEntity<?> getRoleCheck(@RequestBody AdStaffRoleRequest adStaffRoleRequest) {
        return adStaffRoleService.getRoleChecked(adStaffRoleRequest);
    }

    @PutMapping("/change-role")
    public ResponseEntity<?> changeStaffRole(@RequestBody AdChangeStaffRoleRequest adChangeStaffRoleRequest) {
        return adStaffRoleService.changeStaffRole(adChangeStaffRoleRequest);
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getFacilities() {
        return adStaffRoleService.getFacilities();
    }
}
