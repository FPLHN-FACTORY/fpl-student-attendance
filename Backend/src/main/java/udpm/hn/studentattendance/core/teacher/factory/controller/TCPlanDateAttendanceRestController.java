package udpm.hn.studentattendance.core.teacher.factory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.factory.service.TCPlanDateAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_PLANDATE_ATTENDANCE_FACTORY_MANAGEMENT)
public class TCPlanDateAttendanceRestController {

    private final TCPlanDateAttendanceService tcPlanDateAttendanceService;

    @GetMapping("/{idPlanDate}")
    public ResponseEntity<?> getDetail(@PathVariable("idPlanDate") String idPlanDate) {
        return tcPlanDateAttendanceService.getDetail(idPlanDate);
    }

    @GetMapping("/{idPlanDate}/list")
    public ResponseEntity<?> getAllList(@PathVariable String idPlanDate, TCFilterPlanDateAttendanceRequest request) {
        request.setIdPlanDate(idPlanDate);
        return tcPlanDateAttendanceService.getAllList(request);
    }

}
