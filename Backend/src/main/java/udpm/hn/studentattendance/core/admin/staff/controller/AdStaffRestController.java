package udpm.hn.studentattendance.core.admin.staff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.service.AdStaffService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@Controller
@RequiredArgsConstructor
@RequestMapping(RoutesConstant.URL_ADMIN_STAFF_MANAGEMENT)
public class AdStaffRestController {
    private final AdStaffService adStaffService;

    @GetMapping
    public ResponseEntity<?> getAllStaffs(AdStaffRequest staffRequest) {
        return adStaffService.getAllStaffByFilter(staffRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable("id") String id) {
        return adStaffService.getStaffById(id);
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@Valid @RequestBody AdCreateUpdateStaffRequest createUpdateStaffRequest) {
        return adStaffService.createStaff(createUpdateStaffRequest);
    }

    @PutMapping()
    public ResponseEntity<?> updateStaff(@Valid @RequestBody AdCreateUpdateStaffRequest createUpdateStaffRequest) {
        return adStaffService.updateStaff(createUpdateStaffRequest);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> changeStaffStatus(@PathVariable("id") String id) {
        return adStaffService.changeStaffStatus(id);
    }
}
