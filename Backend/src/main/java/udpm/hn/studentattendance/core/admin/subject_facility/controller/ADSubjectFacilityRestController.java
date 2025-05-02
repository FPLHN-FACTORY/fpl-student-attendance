package udpm.hn.studentattendance.core.admin.subject_facility.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.core.admin.subject_facility.service.Admin_SubjectFacilityService;
import udpm.hn.studentattendance.core.admin.subject_facility.service.impl.Admin_FacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

import java.util.List;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT)
public class ADSubjectFacilityRestController {

    @Autowired
    private Admin_SubjectFacilityService adminSubjectFacilityService;

    @Autowired
    private Admin_FacilityManagementService adminFacilityManagementService;

    @GetMapping("/facility-combobox")
    public List<ADFacilityResponse> getFacility() {
        return adminFacilityManagementService.getComboboxFacility();
    }

    @PostMapping("/facility-combobox")
    public List<ADFacilityResponse> getFacilitySubject(@RequestBody ADSubjectFacilitySearchRequest request) {
        return adminFacilityManagementService.getComboboxFacilitySubject(request);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getListSubjectFacility(@RequestBody ADSubjectFacilitySearchRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.getListSubjectFacility(request));
    }

    @PostMapping
    public ResponseEntity<?> addSubjectFacility(@RequestBody ADSubjectFacilityCreateRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.createSubjectFacility(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubjectFacility(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.changeStatus(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectFacility(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.detailSubjectFacility(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubjectFacility(@PathVariable String id,
            @RequestBody ADSubjectFacilityUpdateRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.updateSubjectFacility(id, request));
    }

}
