package udpm.hn.studentattendance.core.teacher.studentattendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.service.TeacherStudentAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_ATTENDANCE)
public class TeacherStudentAttendanceRestController {

    private final TeacherStudentAttendanceService service;


    @PostMapping("{planDateId}")
    public ResponseEntity<?> createBulk(@PathVariable("planDateId") String planDateId) {
        return service.createAttendance(planDateId);
    }


    @GetMapping("/show/{planDateId}")
    public ResponseEntity<?> show(@PathVariable("planDateId") String planDateId) {
        return service.getAllByPlanDate(planDateId);
    }


    @PutMapping()
    public ResponseEntity<?> updateStatus(@RequestBody TeacherModifyStudentAttendanceRequest req) {
        return service.updateStatusAttendance(req);
    }
}
