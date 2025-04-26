package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.student_factory.*;

public interface Staff_StudentFactoryService {
    ResponseEntity<?> getAllStudentInFactory(String factoryId, Staff_StudentFactoryRequest studentFactoryRequest);

    ResponseEntity<?> deleteStudentInFactory(String userStudentFactoryId);

    ResponseEntity<?> changeStatus(String userStudentFactoryId);

    ResponseEntity<?> createOrDeleteStudentFactory(
            Staff_StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest);

    ResponseEntity<?> getStudentFactoryExist(String factoryId);

    ResponseEntity<?> getAllStudent(Staff_UserStudentRequest userStudentRequest);

    ResponseEntity<?> createStudent(Staff_StudentFactoryAddRequest staffStudentFactoryAddRequest);

    ResponseEntity<?> detailStudentFactory(String userStudentId);

    ResponseEntity<?> getAllPlanDateByStudent(Staff_PDDetailShiftByStudentRequest request, String userStudentId);
}
