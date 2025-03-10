package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;

public interface FactoryService {
    ResponseEntity<?> getAllFactory(FactoryRequest staffFactoryRequest);

    ResponseEntity<?> getAllProject();

    ResponseEntity<?> getAllSubjectFacility();

    ResponseEntity<?> getAllStaff();

    ResponseEntity<?> getDetailFactory(String factoryId);

    ResponseEntity<?> createFactory(FactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> updateFactory(FactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> changeStatus(String factoryId);
}
