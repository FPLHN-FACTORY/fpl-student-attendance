package udpm.hn.studentattendance.core.staff.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;
import udpm.hn.studentattendance.core.staff.project.service.STLevelProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STSemesterManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STSubjectFacilityManagementService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PROJECT_MANAGEMENT)
public class USProjectManagementRestController {

    private final SessionHelper sessionHelper;

    private final STProjectManagementService service;

    private final STLevelProjectManagementService serviceLevel;

    private final STSemesterManagementService serviceSemester;

    private final STSubjectFacilityManagementService serviceSubjectFacility;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@Valid @RequestBody USProjectSearchRequest request) {
        request.setFacilityId(sessionHelper.getFacilityId());
        return service.getListProject(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id) {
        return service.detailProject(id);
    }

    @PostMapping
    public ResponseEntity<?> addProject(@Valid @RequestBody USProjectCreateOrUpdateRequest request) {
        return service.createProject(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id, @Valid @RequestBody USProjectCreateOrUpdateRequest request) {
        return service.updateProject(id, request);
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatusProject(@PathVariable String id) {
        return service.changeStatus(id);
    }

    // Get data show combobox
    @GetMapping("/level-combobox")
    public List<USLevelProjectResponse> getLevelProject() {
        return serviceLevel.getComboboxLevelProject();
    }

    @GetMapping("/semester-combobox")
    public List<USSemesterResponse> getSemester() {
        return serviceSemester.getComboboxSemester();
    }

    @GetMapping("/semester")
    public List<Semester> getAllSemester() {
        return serviceSemester.getSemester();
    }

    @GetMapping("/subject-facility-combobox")
    public List<USSubjectResponse> getSubjectFacility() {
        return serviceSubjectFacility.getComboboxSubjectFacility(sessionHelper.getFacilityId());
    }

    @PutMapping("/change-status-semester")
    public ResponseEntity<?> changeStatusProjectPreviousSemester(){
        return service.changeAllStatusPreviousSemester();
    }

}
