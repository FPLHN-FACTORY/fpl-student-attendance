package udpm.hn.studentattendance.core.staff.projectmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.StaffProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.response.ProjectResponse;
import udpm.hn.studentattendance.core.staff.projectmanagement.repository.StaffProjectManagementRepository;
import udpm.hn.studentattendance.core.staff.projectmanagement.service.StaffProjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import java.util.List;

@RestController
@RequestMapping(RoutesConstant.URL_API_STAFF_PROJECT_MANAGEMENT)
public class StaffProjectManagementRestController {
    @Autowired
    private StaffProjectManagementService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@RequestBody StaffProjectSearchRequest request){
        return PaginationHelper.createResponseEntity(service.getListProject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailProject(id));
    }

    @PostMapping
    public ResponseEntity<?> addProject(@RequestBody ProjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createProject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody ProjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateProject(id, request));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.deleteProject(id));
    }
}
