package udpm.hn.studentattendance.core.student.attendance.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;

public interface SAAttendanceService {

    ResponseEntity<?> getAllList(SAFilterAttendanceRequest request);

    ResponseEntity<?> checkin(SACheckinAttendanceRequest request, MultipartFile image);

}
