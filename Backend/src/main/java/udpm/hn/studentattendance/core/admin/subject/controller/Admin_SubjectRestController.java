package udpm.hn.studentattendance.core.admin.subject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.service.Admin_SubjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_MANAGEMENT)
public class Admin_SubjectRestController {

    @Autowired
    private Admin_SubjectManagementService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListSubject(@RequestBody Admin_SubjectSearchRequest request) {
        return PaginationHelper.createResponseEntity(service.getListSubject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailSubject(id));
    }

    @PostMapping
    public ResponseEntity<?> addSubject(@RequestBody Admin_SubjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createSubject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable String id, @RequestBody Admin_SubjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.deleteSubject(id));
    }

}
