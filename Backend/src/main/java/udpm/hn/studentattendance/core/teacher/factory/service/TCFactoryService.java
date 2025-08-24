package udpm.hn.studentattendance.core.teacher.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;

public interface TCFactoryService {

    ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest);

    ResponseEntity<?> getAllProjectByFacility();

    ResponseEntity<?> getAllPlanDateByFactory();

    ResponseEntity<?> getAllSemester();
    
}
