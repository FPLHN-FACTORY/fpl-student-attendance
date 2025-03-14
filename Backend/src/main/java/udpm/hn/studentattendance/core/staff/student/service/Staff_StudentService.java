package udpm.hn.studentattendance.core.staff.student.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentRequest;

public interface Staff_StudentService {
    ResponseEntity<?> getAllStudentByFacility(Staff_StudentRequest studentRequest);

    ResponseEntity<?> getDetailStudent(String studentId);

    ResponseEntity<?> createStudent( Staff_StudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> updateStudent( Staff_StudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> changeStatusStudent(String studentId);


}
