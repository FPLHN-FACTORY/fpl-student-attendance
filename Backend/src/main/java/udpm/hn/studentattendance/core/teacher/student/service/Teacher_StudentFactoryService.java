package udpm.hn.studentattendance.core.teacher.student.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_StudentFactoryRequest;

public interface Teacher_StudentFactoryService {
    ResponseEntity<?> getAllStudentFactory(Teacher_StudentFactoryRequest studentRequest);

    ResponseEntity<?> deleteStudentFactoryById(String studentFactoryId);

    ResponseEntity<?> changeStatusStudentFactory(String studentFactoryId);

    ResponseEntity<?> deleteFaceStudentFactory(String studentId);
}
