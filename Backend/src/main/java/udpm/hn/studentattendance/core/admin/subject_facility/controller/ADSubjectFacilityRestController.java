package udpm.hn.studentattendance.core.admin.subject_facility.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.core.admin.subject_facility.service.Admin_SubjectFacilityService;
import udpm.hn.studentattendance.core.admin.subject_facility.service.Admin_FacilityManagementService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT)
public class ADSubjectFacilityRestController {

    private final Admin_SubjectFacilityService adminSubjectFacilityService;

    private final Admin_FacilityManagementService adminFacilityManagementService;

    @GetMapping("/facility-combobox")
    public ResponseEntity<?> getFacility() {
        return adminFacilityManagementService.getComboboxFacility();
    }

    @PostMapping("/facility-combobox")
    public ResponseEntity<?> getFacilitySubject(@RequestBody ADSubjectFacilitySearchRequest request) {
        return adminFacilityManagementService.getComboboxFacilitySubject(request);
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
