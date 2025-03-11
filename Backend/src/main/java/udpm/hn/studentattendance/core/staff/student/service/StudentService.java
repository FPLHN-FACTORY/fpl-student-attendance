package udpm.hn.studentattendance.core.staff.student.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.student.model.request.StudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.StudentRequest;

public interface StudentService {
    ResponseEntity<?> getAllStudentByFacility(StudentRequest studentRequest);

    ResponseEntity<?> getDetailStudent(String studentId);

    ResponseEntity<?> createStudent( StudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> updateStudent( StudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> changeStatusStudent(String studentId);


}
