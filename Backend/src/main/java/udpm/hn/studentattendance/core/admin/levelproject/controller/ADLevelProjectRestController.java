package udpm.hn.studentattendance.core.admin.levelproject.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.service.ADLevelProjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_LEVEL_PROJECT_MANAGEMENT)
public class ADLevelProjectRestController {

    @Autowired
    private ADLevelProjectManagementService service;

    @GetMapping("/list")
    public ResponseEntity<?> getListLevelProject(@Valid ADLevelProjectSearchRequest request) {
        return service.getListLevelProject(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLevelProject(@PathVariable String id) {
        return service.detailLevelProject(id);
    }

    @PostMapping
    public ResponseEntity<?> addLevelProject(@Valid @RequestBody ADLevelProjectCreateRequest request) {
        return service.createLevelProject(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevelProject(@PathVariable String id,
            @Valid @RequestBody ADLevelProjectUpdateRequest request) {
        return service.updateLevelProject(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLevelProject(@PathVariable String id) {
        return service.changeStatus(id);
    }

}
