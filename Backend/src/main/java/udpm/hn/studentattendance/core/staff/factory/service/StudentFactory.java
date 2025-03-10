package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;

public interface StudentFactory {
    ResponseEntity<?> getAllStudentInFactory();
}
