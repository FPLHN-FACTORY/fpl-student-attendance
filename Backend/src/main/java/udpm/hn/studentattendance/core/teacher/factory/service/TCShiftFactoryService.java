package udpm.hn.studentattendance.core.teacher.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;

public interface TCShiftFactoryService {

    ResponseEntity<?> getDetail(String idFactory);

    ResponseEntity<?> getAllList(TCFilterShiftFactoryRequest request);

}
