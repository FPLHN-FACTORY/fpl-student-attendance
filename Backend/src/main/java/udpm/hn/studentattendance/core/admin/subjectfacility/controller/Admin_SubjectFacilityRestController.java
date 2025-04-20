package udpm.hn.studentattendance.core.admin.subjectfacility.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.Admin_FacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.Admin_SubjectFacilityService;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.impl.Admin_FacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

import java.util.List;


@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_FACILITY_MANAGEMENT)
public class Admin_SubjectFacilityRestController {

    @Autowired
    private Admin_SubjectFacilityService adminSubjectFacilityService;

    @Autowired
    private Admin_FacilityManagementService adminFacilityManagementService;

    @GetMapping("/facility-combobox")
    public List<Admin_FacilityResponse> getFacility() {
        return adminFacilityManagementService.getComboboxFacility();
    }


    @PostMapping("/facility-combobox")
    public List<Admin_FacilityResponse> getFacilitySubject(@RequestBody Admin_SubjectFacilitySearchRequest request) {
        return adminFacilityManagementService.getComboboxFacilitySubject(request);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getListSubjectFacility(@RequestBody Admin_SubjectFacilitySearchRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.getListSubjectFacility(request));
    }

    @PostMapping
    public ResponseEntity<?> addSubjectFacility(@RequestBody Admin_SubjectFacilityCreateRequest request) {
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
    public ResponseEntity<?> updateSubjectFacility(@PathVariable String id, @RequestBody Admin_SubjectFacilityUpdateRequest request) {
        return PaginationHelper.createResponseEntity(adminSubjectFacilityService.updateSubjectFacility(id, request));
    }

}
