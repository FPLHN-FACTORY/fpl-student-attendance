package udpm.hn.studentattendance.core.staff.useractivity.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;

public interface STUserActivityService {

    ResponseEntity<?> getAllUserActivity(UALFilterRequest request);


    ResponseEntity<?> getAllUserStaff();
}
