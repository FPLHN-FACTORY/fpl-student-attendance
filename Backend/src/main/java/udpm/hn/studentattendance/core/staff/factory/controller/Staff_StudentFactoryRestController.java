package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_UserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.service.StudentFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STUDENT_FACTORY_MANAGEMENT)
public class Staff_StudentFactoryRestController {
    private final StudentFactoryService studentFactoryService;

    @GetMapping("/{factoryId}")
    public ResponseEntity<?> getAllStudentInFactory(@PathVariable("factoryId") String factoryId, Staff_StudentFactoryRequest studentFactoryRequest) {
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
    public ResponseEntity<?> getAllStudent(Staff_UserStudentRequest userStudentRequest) {
        return studentFactoryService.getAllStudent(userStudentRequest);
    }

    @PostMapping
    public ResponseEntity<?> addOrDeleteStudentInFactory
            (@Valid @RequestBody Staff_StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
        return studentFactoryService.createOrDeleteStudentFactory(studentFactoryCreateUpdateRequest);
    }
}
