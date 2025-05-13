package udpm.hn.studentattendance.core.teacher.factory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.service.TCStudentFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_FACTORY_MANAGEMENT)
public class TCStudentFactoryRestController {
    private final TCStudentFactoryService teacherStudentService;

    @GetMapping
    public ResponseEntity<?> getAllStudentFactory(TCStudentFactoryRequest studentFactoryRequest) {
        return teacherStudentService.getAllStudentFactory(studentFactoryRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudentFactory(@PathVariable("id") String studentFactoryId) {
        return teacherStudentService.deleteStudentFactoryById(studentFactoryId);
    }

}
