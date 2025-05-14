package udpm.hn.studentattendance.core.teacher.factory.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;

public interface TCPlanDateAttendanceService {

    ResponseEntity<?> getDetail(String idPlanDate);

    ResponseEntity<?> getAllList(TCFilterPlanDateAttendanceRequest request);

}
