package udpm.hn.studentattendance.core.student.history_attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.service.HistoryAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT)
public class HistoryAttendanceRestController {
    private final HistoryAttendanceService historyAttendanceService;

    @GetMapping
    public ResponseEntity<?> getAllAttendanceHistory(HistoryAttendanceRequest historyAttendanceRequest){
        return historyAttendanceService.getAllHistoryAttendanceByStudent(historyAttendanceRequest);
    }


}
