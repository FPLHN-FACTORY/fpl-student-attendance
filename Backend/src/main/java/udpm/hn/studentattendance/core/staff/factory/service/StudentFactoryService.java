package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.UserStudentRequest;

public interface StudentFactoryService {
    ResponseEntity<?> getAllStudentInFactory(String factoryId,StudentFactoryRequest studentFactoryRequest);

    ResponseEntity<?> deleteStudentInFactory(String userStudentFactoryId);

    ResponseEntity<?> changeStatus(String userStudentFactoryId);

    ResponseEntity<?> createOrDeleteStudentFactory(StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest);

    ResponseEntity<?> getStudentFactoryExist();

    ResponseEntity<?> getAllStudent(UserStudentRequest userStudentRequest);

}
