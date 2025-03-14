package udpm.hn.studentattendance.core.admin.semester.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.semester.model.request.Admin_CreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.Admin_SemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.service.Admin_SemesterService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_SEMESTER_MANAGEMENT)
public class Admin_SemesterRestController {
    private final Admin_SemesterService semesterService;

    @GetMapping
    public ResponseEntity<?> getAll(Admin_SemesterRequest request) {
        return semesterService.getAllSemester(request);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<?> getSemesterById
            (@PathVariable("semesterId") String semesterId) {
        return semesterService.getSemesterById(semesterId);
    }

    @PostMapping
    public ResponseEntity<?> createSemester
            (@RequestBody Admin_CreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.createSemester(createUpdateSemesterRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateSemester
            (@RequestBody Admin_CreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.updateSemester(createUpdateSemesterRequest);
    }

    @PutMapping("/status/{semesterId}")
    public ResponseEntity<?> changeStatusSemester
            (@PathVariable("semesterId") String semesterId) {
        return semesterService.changeStatusSemester(semesterId);
    }

}
