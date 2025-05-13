package udpm.hn.studentattendance.core.staff.plan.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PLAN_DATE_ATTENDANCE_MANAGEMENT)
public class SPDPlanDateAttendanceRestController {

    private final SPDPlanDateAttendanceService spdPlanDateAttendanceService;

    @GetMapping("/{idPlanDate}")
    public ResponseEntity<?> getDetail(@PathVariable("idPlanDate") String idPlanDate) {
        return spdPlanDateAttendanceService.getDetail(idPlanDate);
    }

    @GetMapping("/{idPlanDate}/list")
    public ResponseEntity<?> getAllList(@PathVariable String idPlanDate, SPDFilterPlanDateAttendanceRequest request) {
        request.setIdPlanDate(idPlanDate);
        return spdPlanDateAttendanceService.getAllList(request);
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@RequestBody SPDModifyPlanDateAttendanceRequest request) {
        return spdPlanDateAttendanceService.changeStatus(request);
    }

}
