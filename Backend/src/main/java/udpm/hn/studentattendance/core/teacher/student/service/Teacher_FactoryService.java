package udpm.hn.studentattendance.core.teacher.student.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_FactoryRequest;

public interface Teacher_FactoryService {

    ResponseEntity<?> getAllFactoryByTeacher(Teacher_FactoryRequest teacherStudentRequest);

    ResponseEntity<?> getAllProjectByFacility();
}
