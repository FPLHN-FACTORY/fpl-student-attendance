package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;

public interface USFactoryService {
    ResponseEntity<?> getAllFactory(USFactoryRequest staffFactoryRequest);

    ResponseEntity<?> getAllProject();

    ResponseEntity<?> getAllSubjectFacility();

    ResponseEntity<?> getAllStaff();

    ResponseEntity<?> getDetailFactory(String factoryId);

    ResponseEntity<?> createFactory(USFactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> updateFactory(USFactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> changeStatus(String factoryId);

    ResponseEntity<?> detailFactory(String factoryId);

    ResponseEntity<?> existsPlanByFactoryId(String factoryId);

    ResponseEntity<?> changeAllStatusByFactory();

    ResponseEntity<?> getAllSemester();
}
