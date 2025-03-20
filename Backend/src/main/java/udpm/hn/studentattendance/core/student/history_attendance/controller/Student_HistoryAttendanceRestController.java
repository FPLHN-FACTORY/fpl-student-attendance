package udpm.hn.studentattendance.core.student.history_attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.Student_HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.service.Student_HistoryAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT)
public class Student_HistoryAttendanceRestController {
    private final Student_HistoryAttendanceService historyAttendanceService;

    @GetMapping
    public ResponseEntity<?> getAllAttendanceHistory(Student_HistoryAttendanceRequest historyAttendanceRequest){
        return historyAttendanceService.getAllHistoryAttendanceByStudent(historyAttendanceRequest);
    }

    @GetMapping("/semesters")
    public ResponseEntity<?> getAllSemester(){
        return historyAttendanceService.getAllSemester();
    }

    @GetMapping("/factories")
    public ResponseEntity<?> getAllFactory(){
        return historyAttendanceService.getAllFactoryByUserStudent();
    }

}
