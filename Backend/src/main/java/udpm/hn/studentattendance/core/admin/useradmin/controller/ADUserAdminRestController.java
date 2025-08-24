package udpm.hn.studentattendance.core.admin.useradmin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminChangePowerShiftRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest;
import udpm.hn.studentattendance.core.admin.useradmin.service.impl.ADUserAdminServiceImpl;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_ADMIN_MANAGEMENT)
public class ADUserAdminRestController {

    private final ADUserAdminServiceImpl userAdminService;

    @GetMapping
    public ResponseEntity<?> getAllUserAdmin(@Valid ADUserAdminRequest request) {
        return userAdminService.getAllUserAdmin(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserAdminById(@PathVariable(name = "id") String id) {
        return userAdminService.getUserAdminById(id);
    }

    @PostMapping
    public ResponseEntity<?> createUserAdmin(@Valid @RequestBody ADUserAdminCreateOrUpdateRequest request) {
        return userAdminService.createUserAdmin(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserAdmin(@Valid @RequestBody ADUserAdminCreateOrUpdateRequest request,
                                             @PathVariable(name = "id") String id) {
        return userAdminService.updateUserAdmin(request, id);
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable(name = "id") String id) {
        return userAdminService.changeStatus(id);
    }

    @GetMapping("/is-myself/{id}")
    public ResponseEntity<?> checkIsMySelf(@PathVariable("id") String id) {
        return userAdminService.isMySelf(id);
    }

    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        return userAdminService.getAllUserStaff();
    }

    @PostMapping("/change-power")
    public ResponseEntity<?> changePowerShift(@RequestBody ADUserAdminChangePowerShiftRequest userAdminChangePowerShiftRequest) {
        return userAdminService.changePowerShift(userAdminChangePowerShiftRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserAdmin(@PathVariable(name = "id") String id){
        return userAdminService.deleteUserAdmin(id);
    }
}
