package udpm.hn.studentattendance.core.teacher.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_StudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.student.service.Teacher_StudentFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_FACTORY_MANAGEMENT)
public class Teacher_StudentFactoryRestController {
    private final Teacher_StudentFactoryService teacherStudentService;

    @GetMapping
    public ResponseEntity<?> getAllStudentFactory(Teacher_StudentFactoryRequest studentFactoryRequest) {
        return teacherStudentService.getAllStudentFactory(studentFactoryRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudentFactory(@PathVariable("id") String studentFactoryId) {
        return teacherStudentService.deleteStudentFactoryById(studentFactoryId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> changeStatusStudentFactory(@PathVariable("id") String studentFactoryId) {
        return teacherStudentService.changeStatusStudentFactory(studentFactoryId);
    }
}
