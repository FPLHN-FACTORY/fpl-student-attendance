package udpm.hn.studentattendance.core.admin.levelproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.AdminLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.service.AdminLevelProjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_LEVEL_PROJECT_MANAGEMENT)
public class AdminLevelProjectResController {

    @Autowired
    private AdminLevelProjectManagementService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListLevelProject(@RequestBody AdminLevelProjectSearchRequest request) {
        return PaginationHelper.createResponseEntity(service.getListLevelProject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLevelProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailLevelProject(id));
    }

    @PostMapping
    public ResponseEntity<?> addLevelProject(@RequestBody LevelProjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createLevelProject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevelProject(@PathVariable String id, @RequestBody LevelProjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateLevelProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLevelProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.deleteLevelProject(id));
    }

}
