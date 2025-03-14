package udpm.hn.studentattendance.core.admin.role.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.role.model.request.Admin_RoleRequest;
import udpm.hn.studentattendance.core.admin.role.service.Admin_RoleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class Admin_RoleRestController {

    private final Admin_RoleService adRoleService;

    @GetMapping
    public ResponseEntity<?> getAllRole(Admin_RoleRequest adRoleRequest) {
        return adRoleService.getAllRole(adRoleRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetailRole(@PathVariable("id") String id) {
        return adRoleService.getDetailRole(id);
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getAllFacilities() {
        return adRoleService.getFacilities();
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> changeRoleStatus(@PathVariable("id") String id) {
        return adRoleService.deleteRole(id);
    }
}
