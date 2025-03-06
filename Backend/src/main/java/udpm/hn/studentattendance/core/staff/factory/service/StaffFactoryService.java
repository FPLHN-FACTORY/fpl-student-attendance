package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.StaffFactoryRequest;

public interface StaffFactoryService {
    ResponseEntity<?> getAllFactory(StaffFactoryRequest staffFactoryRequest);
}
