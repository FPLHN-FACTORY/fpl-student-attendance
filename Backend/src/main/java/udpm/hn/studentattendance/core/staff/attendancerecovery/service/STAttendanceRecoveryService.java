package udpm.hn.studentattendance.core.staff.attendancerecovery.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;

public interface STAttendanceRecoveryService {

    ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request);

    ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request);

    ResponseEntity<?> getDetailEventAttendanceRecovery(String idEventAttendanceRecovery);

    ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id);

    ResponseEntity<?> importAttendanceRecoveryStudent(STStudentAttendanceRecoveryAddRequest request);

    ResponseEntity<?> getAllHistoryLogByEvent(String idImportLog, EXDataRequest request);

    ResponseEntity<?> getAllHistoryLogDetailEvent(String idImportLog);

    Integer getAllImportStudentSuccess(String idImportLog, String userId, String facilityId, Integer type);

    ResponseEntity<?> isHasStudentAttendanceRecovery(String idAttendanceRecovery);

    ResponseEntity<?> deleteAttendanceRecordByAttendanceRecovery(String idAttendanceRecovery);

}
