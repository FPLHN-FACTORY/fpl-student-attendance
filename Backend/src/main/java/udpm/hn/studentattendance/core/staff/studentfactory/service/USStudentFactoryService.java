package udpm.hn.studentattendance.core.staff.studentfactory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.*;

public interface USStudentFactoryService {
    ResponseEntity<?> getAllStudentInFactory(String factoryId, USStudentFactoryRequest studentFactoryRequest);

    ResponseEntity<?> deleteStudentInFactory(String userStudentFactoryId);

    ResponseEntity<?> changeStatus(String userStudentFactoryId);

    ResponseEntity<?> createOrDeleteStudentFactory(
            USStudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest);

    ResponseEntity<?> getStudentFactoryExist(String factoryId);

    ResponseEntity<?> getAllStudent(USUserStudentRequest userStudentRequest);

    ResponseEntity<?> createStudent(USStudentFactoryAddRequest staffStudentFactoryAddRequest);

    ResponseEntity<?> detailStudentFactory(String userStudentId);

    ResponseEntity<?> getAllPlanDateByStudent(USPDDetailShiftByStudentRequest request, String userStudentId);
}
