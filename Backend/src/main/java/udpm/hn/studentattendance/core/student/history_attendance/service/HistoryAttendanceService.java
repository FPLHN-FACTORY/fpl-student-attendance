package udpm.hn.studentattendance.core.student.history_attendance.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.HistoryAttendanceRequest;

public interface HistoryAttendanceService {

    ResponseEntity<?> getAllHistoryAttendanceByStudent(HistoryAttendanceRequest historyAttendanceRequest);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> getAllFactoryByUserStudent();
}
