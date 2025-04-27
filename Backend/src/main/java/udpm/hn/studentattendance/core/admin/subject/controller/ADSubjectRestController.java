package udpm.hn.studentattendance.core.admin.subject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_SUBJECT_MANAGEMENT)
public class ADSubjectRestController {

    @Autowired
    private ADSubjectManagementService service;

    @GetMapping("/list")
    public ResponseEntity<?> getListSubject(ADSubjectSearchRequest request) {
        return PaginationHelper.createResponseEntity(service.getListSubject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailSubject(id));
    }

    @PostMapping
    public ResponseEntity<?> addSubject(@RequestBody ADSubjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createSubject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable String id, @RequestBody ADSubjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.changeStatus(id));
    }

}
