package udpm.hn.studentattendance.core.admin.subjectfacility.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADSubjectFacilityService;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADFacilityManagementService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT)
public class ADSubjectFacilityRestController {

    private final ADSubjectFacilityService adminSubjectFacilityService;

    private final ADFacilityManagementService adminFacilityManagementService;

    @GetMapping("/facility-combobox/{idSubject}")
    public ResponseEntity<?> getFacility(@PathVariable String idSubject) {
        return adminFacilityManagementService.getComboboxFacility(idSubject);
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getFacilities() {
        return adminFacilityManagementService.getListFacility();
    }

    @PostMapping("/list")
    public ResponseEntity<?> getListSubjectFacility(@RequestBody ADSubjectFacilitySearchRequest request) {
        return adminSubjectFacilityService.getListSubjectFacility(request);
    }

    @PostMapping
    public ResponseEntity<?> addSubjectFacility(@Valid @RequestBody ADSubjectFacilityCreateRequest request) {
        return adminSubjectFacilityService.createSubjectFacility(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubjectFacility(@PathVariable String id) {
        return adminSubjectFacilityService.changeStatus(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectFacility(@PathVariable String id) {
        return adminSubjectFacilityService.detailSubjectFacility(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubjectFacility(@PathVariable String id,
            @Valid @RequestBody ADSubjectFacilityUpdateRequest request) {
        return adminSubjectFacilityService.updateSubjectFacility(id, request);
    }

}
