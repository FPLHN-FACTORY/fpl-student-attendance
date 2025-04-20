package udpm.hn.studentattendance.core.teacher.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_FactoryRequest;
import udpm.hn.studentattendance.core.teacher.student.service.Teacher_FactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_MANAGEMENT)
public class Teacher_FactoryRestController {

    private final Teacher_FactoryService teacherStudentService;

    @GetMapping
    public ResponseEntity<?> getAllFactoryByTeacher(Teacher_FactoryRequest teacherStudentRequest) {
        return teacherStudentService.getAllFactoryByTeacher(teacherStudentRequest);
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjectByFacility() {
        return teacherStudentService.getAllProjectByFacility();
    }
}
