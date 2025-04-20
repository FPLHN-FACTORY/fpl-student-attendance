package udpm.hn.studentattendance.core.staff.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.Staff_LevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.Staff_SemesterResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.Staff_SubjectResponse;
import udpm.hn.studentattendance.core.staff.project.service.Staff_ProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.Staff_LevelProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.Staff_SemesterManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.Staff_SubjectFacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

import java.util.List;

@RestController
@RequestMapping(RouteStaffConstant.URL_API_PROJECT_MANAGEMENT)
public class Staff_ProjectManagementRestController {

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private Staff_ProjectManagementService service;

    @Autowired
    private Staff_LevelProjectManagementService serviceLevel;

    @Autowired
    private Staff_SemesterManagementService serviceSemester;

    @Autowired
    private Staff_SubjectFacilityManagementService serviceSubjectFacility;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@RequestBody Staff_ProjectSearchRequest request) {
        request.setFacilityId(sessionHelper.getFacilityId());
        return PaginationHelper.createResponseEntity(service.getListProject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.detailProject(id));
    }

    @PostMapping
    public ResponseEntity<?> addProject(@RequestBody Staff_ProjectCreateRequest request) {
        return PaginationHelper.createResponseEntity(service.createProject(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody Staff_ProjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.changeStatus(id));
    }

    //Get data show combobox
    @GetMapping("/level-combobox")
    public List<Staff_LevelProjectResponse> getLevelProject() {
        return serviceLevel.getComboboxLevelProject();
    }

    @GetMapping("/semester-combobox")
    public List<Staff_SemesterResponse> getSemester() {
        return serviceSemester.getComboboxSemester(sessionHelper.getFacilityId());
    }

    @GetMapping("/subject-facility-combobox")
    public List<Staff_SubjectResponse> getSubjectFacility() {
        return serviceSubjectFacility.getComboboxSubjectFacility(sessionHelper.getFacilityId());
    }

}
