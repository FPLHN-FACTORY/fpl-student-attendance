package udpm.hn.studentattendance.core.admin.semester.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.service.AdSemesterService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_SEMESTER_MANAGEMENT)
public class AdSemesterRestController {
    private final AdSemesterService semesterService;

    @GetMapping
    public ResponseEntity<?> getAll(AdSemesterRequest request) {
        return semesterService.getAllSemester(request);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<?> getSemesterById
            (@PathVariable("semesterId") String semesterId) {
        return semesterService.getSemesterById(semesterId);
    }

    @PostMapping
    public ResponseEntity<?> createSemester
            (@RequestBody AdCreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.createSemester(createUpdateSemesterRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateSemester
            (@RequestBody AdCreateUpdateSemesterRequest createUpdateSemesterRequest) {
        return semesterService.updateSemester(createUpdateSemesterRequest);
    }

    @PutMapping("/status/{semesterId}")
    public ResponseEntity<?> changeStatusSemester
            (@PathVariable("semesterId") String semesterId) {
        return semesterService.changeStatusSemester(semesterId);
    }

}
