package udpm.hn.studentattendance.core.student.history_attendance.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.Student_HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.model.response.Student_HistoryAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TeachingScheduleResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface Student_HistoryAttendanceService {

    ResponseEntity<?> getAllHistoryAttendanceByStudent(Student_HistoryAttendanceRequest historyAttendanceRequest);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> getAllFactoryByUserStudent();

    ByteArrayInputStream exportHistoryAttendance(List<Student_HistoryAttendanceResponse> attendanceResponses,
            String factoryName);

}
