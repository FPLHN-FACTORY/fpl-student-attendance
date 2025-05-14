package udpm.hn.studentattendance.core.teacher.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCPlanDateStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;

public interface TCStudentFactoryService {

    ResponseEntity<?> getAllStudentFactory(TCStudentFactoryRequest studentRequest);

    ResponseEntity<?> getDetailAttendance(TCPlanDateStudentFactoryRequest request);

    ResponseEntity<?> deleteStudentFactoryById(String studentFactoryId);

}
