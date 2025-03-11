package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.UserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.service.StudentFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STUDENT_FACTORY_MANAGEMENT)
public class StudentFactoryRestController {
    private final StudentFactoryService studentFactoryService;

    @GetMapping("/{factoryId}")
    public ResponseEntity<?> getAllStudentInFactory(@PathVariable("factoryId") String factoryId, StudentFactoryRequest studentFactoryRequest) {
        return studentFactoryService.getAllStudentInFactory(factoryId, studentFactoryRequest);
    }

    @DeleteMapping("/{studentFactoryId}")
    public ResponseEntity<?> deleteStudentFactory(@PathVariable("studentFactoryId") String studentFactoryId) {
        return studentFactoryService.deleteStudentInFactory(studentFactoryId);
    }

    @PutMapping("/{studentFactoryId}")
    public ResponseEntity<?> changeStatusStudentFactory(@PathVariable("studentFactoryId") String studentFactoryId) {
        return studentFactoryService.changeStatus(studentFactoryId);
    }

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudent(UserStudentRequest userStudentRequest) {
        return studentFactoryService.getAllStudent(userStudentRequest);
    }

    @PostMapping
    public ResponseEntity<?> addOrDeleteStudentInFactory
            (@Valid @RequestBody StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
        return studentFactoryService.createOrDeleteStudentFactory(studentFactoryCreateUpdateRequest);
    }
}
