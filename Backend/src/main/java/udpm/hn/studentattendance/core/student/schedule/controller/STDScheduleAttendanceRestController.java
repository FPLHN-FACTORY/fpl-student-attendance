package udpm.hn.studentattendance.core.student.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.core.student.schedule.service.STDScheduleAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_SCHEDULE_MANAGEMENT)
public class STDScheduleAttendanceRestController {

    private final SessionHelper sessionHelper;

    private final STDScheduleAttendanceService service;

    private final STDScheduleAttendanceRepository repository;

    @GetMapping("/list")
    public ResponseEntity<?> getListProject(STDScheduleAttendanceSearchRequest request) {
        request.setIdStudent(sessionHelper.getUserId());
        return PaginationHelper.createResponseEntity(service.getList(request));
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> exportTeachingSchedule(STDScheduleAttendanceSearchRequest request) throws UnsupportedEncodingException, IOException {
        request.setIdStudent(sessionHelper.getUserId());
        List<STDScheduleAttendanceResponse> list = repository
                .getAllListAttendanceByUserList(request);
        ByteArrayInputStream byteArrayInputStream = service.exportScheduleAttendance(list);
        HttpHeaders headers = new HttpHeaders();
        String fileName = "lịch-học.pdf";
        headers.add("Content-Disposition",
                "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(byteArrayInputStream));
    }

}
