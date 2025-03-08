package udpm.hn.studentattendance.core.admin.subjectfacility.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.AdminSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.AdminSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.AdminSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.AdminFacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.AdminSubjectFacilityService;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.impl.AdminFacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

import java.util.List;


@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT)
public class AdminSubjectFacilityRestController {

    @Autowired
    private AdminSubjectFacilityService adminSubjectFacilityService;

    @Autowired
    private AdminFacilityManagementService adminFacilityManagementService;

    @GetMapping("/facility-combobox")
    public List<AdminFacilityResponse> getFacility() {
        return adminFacilityManagementService.getComboboxFacility();
    }


    @PostMapping("/facility-combobox")
    public List<AdminFacilityResponse> getFacilitySubject(@RequestBody AdminSubjectFacilitySearchRequest request) {
        return adminFacilityManagementService.getComboboxFacilitySubject(request);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getListSubjectFacility(@RequestBody AdminSubjectFacilitySearchRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.getListSubjectFacility(request));
    }

    @PostMapping
    public ResponseEntity<?> addSubjectFacility(@RequestBody AdminSubjectFacilityCreateRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.createSubjectFacility(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubjectFacility(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.deleteSubjectFacility(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectFacility(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.detailSubjectFacility(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubjectFacility(@PathVariable String id, @RequestBody AdminSubjectFacilityUpdateRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.updateSubjectFacility(id, request));
    }

}
