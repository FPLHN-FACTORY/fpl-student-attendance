package udpm.hn.studentattendance.core.admin.staff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_CreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRequest;
import udpm.hn.studentattendance.core.admin.staff.service.Admin_StaffService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_STAFF_MANAGEMENT)
public class Admin_StaffRestController {
    private final Admin_StaffService adStaffService;

    @GetMapping
    public ResponseEntity<?> getAllStaffs(Admin_StaffRequest staffRequest) {
        return adStaffService.getAllStaffByFilter(staffRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable("id") String id) {
        return adStaffService.getStaffById(id);
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@Valid @RequestBody Admin_CreateUpdateStaffRequest createUpdateStaffRequest) {
        return adStaffService.createStaff(createUpdateStaffRequest);
    }

    @PutMapping()
    public ResponseEntity<?> updateStaff(@Valid @RequestBody Admin_CreateUpdateStaffRequest createUpdateStaffRequest) {
        return adStaffService.updateStaff(createUpdateStaffRequest);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> changeStaffStatus(@PathVariable("id") String id) {
        return adStaffService.changeStaffStatus(id);
    }

}
