package udpm.hn.studentattendance.core.teacher.factory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.service.TCFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_FACTORY_MANAGEMENT)
public class TCFactoryRestController {

    private final TCFactoryService teacherStudentService;

    @GetMapping
    public ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest) {
        return teacherStudentService.getAllFactoryByTeacher(teacherStudentRequest);
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjectByFacility() {
        return teacherStudentService.getAllProjectByFacility();
    }

    @GetMapping("/semesters")
    public ResponseEntity<?> getAllSemester() {
        return teacherStudentService.getAllSemester();
    }

}
