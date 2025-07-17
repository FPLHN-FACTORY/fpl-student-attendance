package udpm.hn.studentattendance.core.admin.semester.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.service.ADSemesterService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_SEMESTER_MANAGEMENT)
public class ADSemesterRestController {
    private final ADSemesterService semesterService;

    @GetMapping
    public ResponseEntity<?> getAll(@Valid ADSemesterRequest request) {
        return semesterService.getAllSemester(request);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<?> getSemesterById(@PathVariable("semesterId") String semesterId) {
        return semesterService.getSemesterById(semesterId);
    }

    @PostMapping
    public ResponseEntity<?> createSemester(
            @RequestBody ADCreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.createSemester(createUpdateSemesterRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateSemester(
            @RequestBody ADCreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.updateSemester(createUpdateSemesterRequest);
    }

    @PutMapping("/status/{semesterId}")
    public ResponseEntity<?> changeStatusSemester(@PathVariable("semesterId") String semesterId) {
        return semesterService.changeStatusSemester(semesterId);
    }

}
