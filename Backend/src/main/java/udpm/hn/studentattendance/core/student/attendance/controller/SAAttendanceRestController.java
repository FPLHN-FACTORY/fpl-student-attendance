package udpm.hn.studentattendance.core.student.attendance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.service.SAAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_ATTENDANCE)
public class SAAttendanceRestController {

    private final SAAttendanceService saAttendanceService;

    @GetMapping
    public ResponseEntity<?> getAllList(SAFilterAttendanceRequest request) {
        return saAttendanceService.getAllList(request);
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@Valid @RequestBody SACheckinAttendanceRequest request) {
        return saAttendanceService.checkin(request);
    }

}
