package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USPDDetailShiftByStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USStudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USStudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USUserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.service.USStudentFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STUDENT_FACTORY_MANAGEMENT)
public class USStudentFactoryRestController {
    private final USStudentFactoryService studentFactoryService;

    @GetMapping("/{factoryId}")
    public ResponseEntity<?> getAllStudentInFactory(@PathVariable("factoryId") String factoryId,
                                                    USStudentFactoryRequest studentFactoryRequest) {
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
    public ResponseEntity<?> getAllStudent(USUserStudentRequest userStudentRequest) {
        return studentFactoryService.getAllStudent(userStudentRequest);
    }

    @PostMapping
    public ResponseEntity<?> addOrDeleteStudentInFactory(
            @Valid @RequestBody USStudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
        return studentFactoryService.createOrDeleteStudentFactory(studentFactoryCreateUpdateRequest);
    }

    @GetMapping("/exist-student/{id}")
    public ResponseEntity<?> getStudentInFactory(@PathVariable String id) {
        return studentFactoryService.getStudentFactoryExist(id);
    }

    @GetMapping("/detail-student/{id}")
    public ResponseEntity<?> detailStudentFactory(@PathVariable String id) {
        return studentFactoryService.detailStudentFactory(id);
    }

    @GetMapping("/detail-shift/{id}")
    public ResponseEntity<?> detailShiftByStudent(USPDDetailShiftByStudentRequest request, @PathVariable String id) {
        return studentFactoryService.getAllPlanDateByStudent(request, id);
    }
}
