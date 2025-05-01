package udpm.hn.studentattendance.core.staff.student.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.service.STStudentService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STUDENT_MANAGEMENT)
public class USStudentRestController {

    private final STStudentService studentService;

    @GetMapping
    public ResponseEntity<?> getAllStudent(USStudentRequest studentRequest) {
        return studentService.getAllStudentByFacility(studentRequest);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable("studentId") String studentId) {
        return studentService.getDetailStudent(studentId);
    }

    @PostMapping
    public ResponseEntity<?> createStudent(
            @Valid @RequestBody USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        return studentService.createStudent(studentCreateUpdateRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateStudent(
            @Valid @RequestBody USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        return studentService.updateStudent(studentCreateUpdateRequest);
    }

    @PutMapping("/status/{studentId}")
    public ResponseEntity<?> changeStatus(@PathVariable("studentId") String studentId) {
        return studentService.changeStatusStudent(studentId);
    }

    @PutMapping("/change-face/{studentId}")
    public ResponseEntity<?> changeFaceStudent(@PathVariable("studentId") String studentId) {
        return studentService.deleteFaceStudentFactory(studentId);
    }

    @GetMapping("/exist-face")
    public ResponseEntity<?> isNotExistFace() {
        return studentService.isExistFace();
    }
}
