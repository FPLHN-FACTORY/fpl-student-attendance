package udpm.hn.studentattendance.core.teacher.teachingschedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.service.TCTeachingScheduleService;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_TEACHING_SCHEDULE_MANAGEMENT)
public class TCTeachingScheduleRestController {

    private final TCTeachingScheduleService service;

    private final TCTeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;

    private final SessionHelper sessionHelper;

    @GetMapping()
    public ResponseEntity<?> getAllTeachingScheduleByStaff(TCTeachingScheduleRequest request) {
        return service.getAllTeachingScheduleByStaff(request);
    }

    @GetMapping("/schedule-present")
    public ResponseEntity<?> getTeachingScheduleByPresent(TCTeachingScheduleRequest request) {
        return service.getAllTeachingSchedulePresent(request);
    }

    @GetMapping("/factories")
    public ResponseEntity<?> getAllFactoriesStaff() {
        return service.getAllFactoryByStaff();
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjectsStaff() {
        return service.getAllProjectByStaff();
    }

    @GetMapping("/subjects")
    public ResponseEntity<?> getAllSubjectsStaff() {
        return service.getAllSubjectByStaff();
    }

    @GetMapping("/{planDateId}")
    public ResponseEntity<?> getDetailPlanDate(@PathVariable("planDateId") String planDateId) {
        return service.getDetailPlanDate(planDateId);
    }

    @PutMapping()
    public ResponseEntity<?> updatePlanDate(@RequestBody TCTSPlanDateUpdateRequest planDateUpdateRequest) {
        return service.updatePlanDate(planDateUpdateRequest);
    }

    @PutMapping("/change-type/{id}")
    public ResponseEntity<?> changeType(@PathVariable("id") String id, @RequestParam String room) {
        return service.changeTypePlanDate(id, room);
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> exportTeachingSchedule(
            TCTeachingScheduleRequest teachingScheduleRequest) throws UnsupportedEncodingException, IOException {
        List<TCTeachingScheduleResponse> list = teacherTeachingScheduleExtendRepository
                .exportExcelTeachingSchedule(sessionHelper.getUserId(), teachingScheduleRequest);
        ByteArrayInputStream byteArrayInputStream = service.exportTeachingSchedule(list);
        HttpHeaders headers = new HttpHeaders();
        String fileName = "lịch-dạy.pdf";
        headers.add("Content-Disposition",
                "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(byteArrayInputStream));
    }

    @GetMapping("/type")
    public ResponseEntity<?> getAllType() {
        return service.getAllType();
    }
}
