package udpm.hn.studentattendance.core.staff.attendancerecovery.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryRequest;

public interface STAttendanceRecoveryService {

    ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request);

    ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request);

    ResponseEntity<?> getDetailEventAttendanceRecovery(String idEventAttendanceRecovery);

    ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id);

    ResponseEntity<?> importAttendanceRecoveryStudent(STStudentAttendanceRecoveryRequest request);

}
