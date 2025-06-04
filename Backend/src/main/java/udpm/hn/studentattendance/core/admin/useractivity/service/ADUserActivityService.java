package udpm.hn.studentattendance.core.admin.useractivity.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;

public interface ADUserActivityService {

    ResponseEntity<?> getAllUserActivity(UALFilterRequest request);

    ResponseEntity<?> getAllFacility();

    ResponseEntity<?> getAllUserAdmin();

    ResponseEntity<?> getAllUserStaff();
}
