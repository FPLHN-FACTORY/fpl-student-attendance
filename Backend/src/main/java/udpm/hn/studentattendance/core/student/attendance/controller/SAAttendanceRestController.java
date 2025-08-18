package udpm.hn.studentattendance.core.student.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
    public ResponseEntity<?> checkin(
            @ModelAttribute SACheckinAttendanceRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "canvas", required = false) MultipartFile canvas,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        request.setSignature(signature);
        return saAttendanceService.checkin(request, image, canvas);
    }

}
