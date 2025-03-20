package udpm.hn.studentattendance.core.student.history_attendance.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.Student_HistoryAttendanceRequest;

public interface Student_HistoryAttendanceService {

    ResponseEntity<?> getAllHistoryAttendanceByStudent(Student_HistoryAttendanceRequest historyAttendanceRequest);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> getAllFactoryByUserStudent();
}
