package udpm.hn.studentattendance.core.admin.semester.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.semester.model.request.CreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.SemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.service.SemesterService;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/semesters")
public class SemesterRestController {
    private SemesterService semesterService;

    @GetMapping
    public ResponseEntity<?> getAll(SemesterRequest request) {
        return semesterService.getAllSemester(request);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<?> getSemesterById(@PathVariable("semesterId") String semesterId) {
        return semesterService.getSemesterById(semesterId);
    }

    @PostMapping
    public ResponseEntity<?> createSemester(CreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.createSemester(createUpdateSemesterRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateSemester(CreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.updateSemester(createUpdateSemesterRequest);
    }

    @PutMapping("/status/{semesterId")
    public ResponseEntity<?> changeStatusSemester(@PathVariable("semesterId") String semesterId) {
        return semesterService.changeStatusSemester(semesterId);
    }
}
