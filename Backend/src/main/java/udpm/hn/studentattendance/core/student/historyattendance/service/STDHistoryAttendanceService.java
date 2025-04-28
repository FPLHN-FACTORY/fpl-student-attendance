package udpm.hn.studentattendance.core.student.historyattendance.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface STDHistoryAttendanceService {

    ResponseEntity<?> getAllHistoryAttendanceByStudent(STDHistoryAttendanceRequest historyAttendanceRequest);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> getAllFactoryByUserStudent();

    ByteArrayInputStream exportHistoryAttendance(List<STDHistoryAttendanceResponse> attendanceResponses,
            String factoryName);

}
