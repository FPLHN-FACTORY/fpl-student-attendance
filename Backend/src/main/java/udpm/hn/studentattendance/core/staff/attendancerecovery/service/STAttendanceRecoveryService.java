package udpm.hn.studentattendance.core.staff.attendancerecovery.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryDeleteRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;

public interface STAttendanceRecoveryService {

    ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request);

    ResponseEntity<?> deleteAttendanceRecovery(STAttendanceRecoveryDeleteRequest recoveryDeleteRequest);


}
