package udpm.hn.studentattendance.core.staff.student.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentRequest;
import udpm.hn.studentattendance.core.staff.student.service.Staff_StudentService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STUDENT_MANAGEMENT)
public class Staff_StudentRestController {

    private final Staff_StudentService studentService;

    @GetMapping
    public ResponseEntity<?> getAllStudent(Staff_StudentRequest studentRequest) {
        return studentService.getAllStudentByFacility(studentRequest);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable("studentId") String studentId) {
        return studentService.getDetailStudent(studentId);
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Staff_StudentCreateUpdateRequest studentCreateUpdateRequest) {
        return studentService.createStudent(studentCreateUpdateRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateStudent(@Valid @RequestBody Staff_StudentCreateUpdateRequest studentCreateUpdateRequest) {
        return studentService.updateStudent(studentCreateUpdateRequest);
    }

    @PutMapping("/status/{studentId}")
    public ResponseEntity<?> changeStatus(@PathVariable("studentId") String studentId) {
        return studentService.changeStatusStudent(studentId);
    }
}
