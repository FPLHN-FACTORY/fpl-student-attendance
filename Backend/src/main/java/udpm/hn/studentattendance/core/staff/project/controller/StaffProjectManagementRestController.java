package udpm.hn.studentattendance.core.staff.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.project.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.StaffProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.SemesterResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.core.staff.project.service.StaffProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.StaffLevelProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.StaffSemesterManagementService;
import udpm.hn.studentattendance.core.staff.project.service.ipml.StaffSubjectFacilityManagementService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

import java.util.List;

@RestController
@RequestMapping(RouteStaffConstant.URL_API_PROJECT_MANAGEMENT)
public class StaffProjectManagementRestController {

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private StaffProjectManagementService service;

    @Autowired
    private StaffLevelProjectManagementService serviceLevel;

    @Autowired
    private StaffSemesterManagementService serviceSemester;

    @Autowired
    private StaffSubjectFacilityManagementService serviceSubjectFacility;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@RequestBody StaffProjectSearchRequest request) {
        request.setFacilityId(sessionHelper.getFacilityId());
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

}
