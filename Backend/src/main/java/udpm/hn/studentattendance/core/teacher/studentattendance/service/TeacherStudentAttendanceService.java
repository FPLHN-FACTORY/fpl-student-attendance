package udpm.hn.studentattendance.core.teacher.studentattendance.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;

public interface TeacherStudentAttendanceService {

    ResponseEntity<?> createAttendance(String request);

    ResponseEntity<?> getAllByPlanDate(String teacherStudentAttendanceRequest);

    ResponseEntity<?> updateStatusAttendance(TeacherModifyStudentAttendanceRequest req);

}
