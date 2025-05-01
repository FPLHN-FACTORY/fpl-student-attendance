package udpm.hn.studentattendance.core.staff.student.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;

public interface STStudentService {
    ResponseEntity<?> getAllStudentByFacility(USStudentRequest studentRequest);

    ResponseEntity<?> getDetailStudent(String studentId);

    ResponseEntity<?> createStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> updateStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest);

    ResponseEntity<?> changeStatusStudent(String studentId);

    ResponseEntity<?> deleteFaceStudentFactory(String studentId);

    ResponseEntity<?> isExistFace();

}
