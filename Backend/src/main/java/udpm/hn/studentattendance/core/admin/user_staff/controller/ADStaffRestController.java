package udpm.hn.studentattendance.core.admin.user_staff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.ADStaffRequest;
import udpm.hn.studentattendance.core.admin.user_staff.service.ADStaffService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_STAFF_MANAGEMENT)
public class ADStaffRestController {
    private final ADStaffService adStaffService;

    @GetMapping
    public ResponseEntity<?> getAllStaffs(ADStaffRequest staffRequest) {
        return adStaffService.getAllStaffByFilter(staffRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable("id") String id) {
        return adStaffService.getStaffById(id);
    }

    @PostMapping()
    public ResponseEntity<?> createStaff(@Valid @RequestBody ADCreateUpdateStaffRequest createUpdateStaffRequest) {
        return adStaffService.createStaff(createUpdateStaffRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@Valid @RequestBody ADCreateUpdateStaffRequest createUpdateStaffRequest,
            @PathVariable(name = "id") String id) {
        return adStaffService.updateStaff(createUpdateStaffRequest, id);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> changeStaffStatus(@PathVariable("id") String id) {
        return adStaffService.changeStaffStatus(id);
    }

    @GetMapping("/role")
    public ResponseEntity<?> getAllRole() {
        return adStaffService.getAllRole();
    }

    @GetMapping("/facility")
    public ResponseEntity<?> getAllFacility() {
        return adStaffService.getAllFacility();
    }

}
