package udpm.hn.studentattendance.core.student.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.service.STDScheduleAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_SCHEDULE_MANAGEMENT)
public class STDScheduleAttendanceRestController {

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private STDScheduleAttendanceService service;

    @GetMapping("/list")
    public ResponseEntity<?> getListProject(STDScheduleAttendanceSearchRequest request) {
        request.setIdStudent(sessionHelper.getUserId());
        return PaginationHelper.createResponseEntity(service.getList(request));
    }

}
