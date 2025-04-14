package udpm.hn.studentattendance.core.teacher.studentattendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.service.TeacherStudentAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_ATTENDANCE)
public class TeacherStudentAttendanceRestController {

    private final TeacherStudentAttendanceService service;

    @GetMapping("/{req}")
    public ResponseEntity<?> create(@PathVariable("req") String req) {
        return service.createAttendance(req);
    }

    @GetMapping("/show/{req}")
    public ResponseEntity<?> show(@PathVariable("req") String req) {
        return service.getAllByPlanDate(req);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody TeacherStudentAttendanceRequest req) {
        return service.updateStatusAttendance(req);
    }

}
