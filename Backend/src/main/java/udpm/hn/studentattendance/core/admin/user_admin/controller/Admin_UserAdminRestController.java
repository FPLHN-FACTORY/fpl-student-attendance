package udpm.hn.studentattendance.core.admin.user_admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminRequest;
import udpm.hn.studentattendance.core.admin.user_admin.service.impl.Admin_UserAdminServiceImpl;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_ADMIN_MANAGEMENT)
public class Admin_UserAdminRestController {

    private final Admin_UserAdminServiceImpl userAdminService;

    @GetMapping
    public ResponseEntity<?> getAllUserAdmin(Admin_UserAdminRequest request) {
        return userAdminService.getAllUserAdmin(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserAdminById(@PathVariable(name = "id") String id) {
        return userAdminService.getUserAdminById(id);
    }

    @PostMapping
    public ResponseEntity<?> createUserAdmin(@Valid @RequestBody Admin_UserAdminCreateOrUpdateRequest request) {
        return userAdminService.createUserAdmin(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserAdmin(@Valid @RequestBody Admin_UserAdminCreateOrUpdateRequest request, @PathVariable(name = "id") String id) {
        return userAdminService.updateUserAdmin(request, id);
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable(name = "id") String id) {
        return userAdminService.changeStatus(id);
    }

}
