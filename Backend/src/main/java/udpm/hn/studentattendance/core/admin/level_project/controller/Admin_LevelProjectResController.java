package udpm.hn.studentattendance.core.admin.level_project.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.level_project.model.request.Admin_LevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.level_project.service.Admin_LevelProjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_LEVEL_PROJECT_MANAGEMENT)
public class Admin_LevelProjectResController {

    @Autowired
    private Admin_LevelProjectManagementService service;

    @GetMapping("/list")
    public ResponseEntity<?> getListLevelProject(@Valid Admin_LevelProjectSearchRequest request) {
        return PaginationHelper.createResponseEntity(service.getListLevelProject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLevelProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailLevelProject(id));
    }

    @PostMapping
    public ResponseEntity<?> addLevelProject(@RequestBody Admin_LevelProjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createLevelProject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevelProject(@PathVariable String id,
            @RequestBody Admin_LevelProjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateLevelProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLevelProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.changeStatus(id));
    }

}
