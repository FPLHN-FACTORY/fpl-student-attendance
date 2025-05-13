package udpm.hn.studentattendance.core.staff.plan.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;

public interface SPDPlanDateAttendanceService {

    ResponseEntity<?> getDetail(String idPlanDate);

    ResponseEntity<?> getAllList(SPDFilterPlanDateAttendanceRequest request);

    ResponseEntity<?> changeStatus(SPDModifyPlanDateAttendanceRequest request);

}
