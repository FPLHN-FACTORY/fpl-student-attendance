package udpm.hn.studentattendance.core.admin.subject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.service.AdminSubjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_MANAGEMENT)
public class AdminSubjectRestController {

    @Autowired
    private AdminSubjectManagementService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListSubject(@RequestBody AdminSubjectSearchRequest request) {
        return PaginationHelper.createResponseEntity(service.getListSubject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailSubject(id));
    }

    @PostMapping
    public ResponseEntity<?> addSubject(@RequestBody AdminSubjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createSubject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable String id, @RequestBody AdminSubjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.deleteSubject(id));
    }

}
