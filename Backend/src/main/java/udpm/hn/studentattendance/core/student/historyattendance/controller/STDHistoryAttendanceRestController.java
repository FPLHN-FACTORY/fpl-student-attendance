package udpm.hn.studentattendance.core.student.historyattendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.historyattendance.repository.STDHistoryAttendanceExtendRepository;
import udpm.hn.studentattendance.core.student.historyattendance.service.STDHistoryAttendanceService;
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
@RequestMapping(RouteStudentConstant.URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT)
public class STDHistoryAttendanceRestController {
    private final STDHistoryAttendanceService historyAttendanceService;

    private final STDHistoryAttendanceExtendRepository attendanceExtendRepository;

    private final SessionHelper sessionHelper;

    @GetMapping
    public ResponseEntity<?> getAllAttendanceHistory(STDHistoryAttendanceRequest historyAttendanceRequest) {
        return historyAttendanceService.getAllHistoryAttendanceByStudent(historyAttendanceRequest);
    }

    @GetMapping("/semesters")
    public ResponseEntity<?> getAllSemester() {
        return historyAttendanceService.getAllSemester();
    }

    @GetMapping("/factories")
    public ResponseEntity<?> getAllFactory() {
        return historyAttendanceService.getAllFactoryByUserStudent();
    }

    @GetMapping("/factories/{idSemester}")
    public ResponseEntity<?> getAllFactoryBySemester(@PathVariable String idSemester) {
        return historyAttendanceService.getAllFactoryBySemester(idSemester);
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> exportTeachingSchedule(@RequestParam String factoryName,
                                                                      @RequestParam String factoryId) throws UnsupportedEncodingException, IOException {
        List<STDHistoryAttendanceResponse> list = attendanceExtendRepository
                .getAllHistoryAttendanceByFactory(sessionHelper.getUserId(), factoryId, System.currentTimeMillis());
        ByteArrayInputStream byteArrayInputStream = historyAttendanceService.exportHistoryAttendance(list, factoryName);
        HttpHeaders headers = new HttpHeaders();
        String fileName = "lịch-sử-điểm-danh.pdf";
        headers.add("Content-Disposition",
                "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(byteArrayInputStream));
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getDetailPlanDate() {
        return historyAttendanceService.getDetailPlanDate();
    }

}
