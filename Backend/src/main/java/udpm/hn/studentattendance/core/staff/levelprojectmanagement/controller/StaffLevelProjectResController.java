package udpm.hn.studentattendance.core.staff.levelprojectmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.StaffLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.service.StaffLevelProjectManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequestMapping(RoutesConstant.URL_API_STAFF_LEVEL_PROJECT_MANAGEMENT)
public class StaffLevelProjectResController {

    @Autowired
    private StaffLevelProjectManagementService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListLevelProject(@RequestBody StaffLevelProjectSearchRequest request) {
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
