package udpm.hn.studentattendance.core.student.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.student.attendance.model.request.StudentAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.attendance.service.StudentAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_MANAGEMENT)
public class StudentAttendanceRestController {

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private StudentAttendanceService service;

    @PostMapping("/list")
    public ResponseEntity<?> getListProject(@RequestBody StudentAttendanceSearchRequest request) {
        request.setIdStudent(sessionHelper.getUserId());
        return PaginationHelper.createResponseEntity(service.getList(request));
    }

}
