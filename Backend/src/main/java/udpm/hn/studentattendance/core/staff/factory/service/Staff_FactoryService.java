package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryRequest;

public interface Staff_FactoryService {
    ResponseEntity<?> getAllFactory(Staff_FactoryRequest staffFactoryRequest);

    ResponseEntity<?> getAllProject();

    ResponseEntity<?> getAllSubjectFacility();

    ResponseEntity<?> getAllStaff();

    ResponseEntity<?> getDetailFactory(String factoryId);

    ResponseEntity<?> createFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> updateFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest);

    ResponseEntity<?> changeStatus(String factoryId);

    ResponseEntity<?> detailFactory(String factoryId);

    ResponseEntity<?> existsPlanByFactoryId(String factoryId);

    ResponseEntity<?> changeAllStatusByFactory();

    ResponseEntity<?> getAllSemester();
}
