package udpm.hn.studentattendance.core.admin.role.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.role.model.request.AdRoleRequest;
import udpm.hn.studentattendance.core.admin.role.service.AdRoleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleRestController {

    private final AdRoleService adRoleService;

    @GetMapping
    public ResponseEntity<?> getAllRole(AdRoleRequest adRoleRequest) {
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
