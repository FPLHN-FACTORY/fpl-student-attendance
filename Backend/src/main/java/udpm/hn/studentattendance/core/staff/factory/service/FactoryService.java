package udpm.hn.studentattendance.core.staff.factory.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;

public interface FactoryService {
    ResponseEntity<?> getAllFactory(FactoryRequest factoryRequest);
}
