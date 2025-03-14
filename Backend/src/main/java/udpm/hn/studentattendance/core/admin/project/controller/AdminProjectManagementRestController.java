package udpm.hn.studentattendance.core.admin.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.AdminProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.project.model.response.FacilityResponse;
import udpm.hn.studentattendance.core.admin.project.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.admin.project.model.response.SemesterResponse;
import udpm.hn.studentattendance.core.admin.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.core.admin.project.service.AdminProjectManagementService;
import udpm.hn.studentattendance.core.admin.project.service.ipml.AdminFacilitySubjectManagementService;
import udpm.hn.studentattendance.core.admin.project.service.ipml.AdminLevelProjectManagementService;
import udpm.hn.studentattendance.core.admin.project.service.ipml.AdminSemesterManagementService;
import udpm.hn.studentattendance.core.admin.project.service.ipml.AdminSubjectFacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

import java.util.List;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_PROJECT_MANAGEMENT)
public class AdminProjectManagementRestController {

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private AdminProjectManagementService service;

    @Autowired
    private AdminLevelProjectManagementService serviceLevel;

    @Autowired
    private AdminSemesterManagementService serviceSemester;

    @Autowired
    private AdminSubjectFacilityManagementService serviceSubjectFacility;

    @Autowired
    private AdminFacilitySubjectManagementService adminFacilitySubjectManagementService;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@RequestBody AdminProjectSearchRequest request) {
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
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody ProjectUpdateRequest request) {
        return PaginationHelper.createResponseEntity(service.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return PaginationHelper.createResponseEntity(service.deleteProject(id));
    }

    //Get data show combobox
    @GetMapping("/level-combobox")
    public List<LevelProjectResponse> getLevelProject() {
        return serviceLevel.getComboboxLevelProject();
    }

    @GetMapping("/semester-combobox")
    public List<SemesterResponse> getSemester() {
        return serviceSemester.getComboboxSemester(sessionHelper.getFacilityId());
    }

    @GetMapping("/subject-facility-combobox")
    public List<SubjectResponse> getSubjectFacility() {
        return serviceSubjectFacility.getComboboxSubjectFacility(sessionHelper.getFacilityId());
    }

    @GetMapping("/subject-combobox-add")
    public List<SubjectResponse> getSubjectAdd() {
        return serviceSubjectFacility.getComboboxSubjectAdd();
    }

    @GetMapping("/facility-combobox/{id}")
    public List<FacilityResponse> getFacility(@PathVariable String id) {
        return adminFacilitySubjectManagementService.getComboboxFacility(id);
    }
}
