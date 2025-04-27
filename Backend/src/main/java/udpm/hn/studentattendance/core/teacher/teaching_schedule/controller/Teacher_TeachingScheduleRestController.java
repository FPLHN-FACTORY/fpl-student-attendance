package udpm.hn.studentattendance.core.teacher.teaching_schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.Teacher_TeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.service.Teacher_TeachingScheduleService;
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
public class Teacher_TeachingScheduleRestController {

    private final Teacher_TeachingScheduleService service;

    private final Teacher_TeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;

    private final SessionHelper sessionHelper;

    @GetMapping()
    public ResponseEntity<?> getAllTeachingScheduleByStaff(Teacher_TeachingScheduleRequest request) {
        return service.getAllTeachingScheduleByStaff(request);
    }

    @GetMapping("/schedule-present")
    public ResponseEntity<?> getTeachingScheduleByPresent(Teacher_TeachingScheduleRequest request) {
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
    public ResponseEntity<?> updatePlanDate(@RequestBody Teacher_TSPlanDateUpdateRequest planDateUpdateRequest) {
        return service.updatePlanDate(planDateUpdateRequest);
    }

    @PutMapping("/change-type/{id}")
    public ResponseEntity<?> changeType(@PathVariable("id") String id) {
        return service.changeTypePlanDate(id);
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> exportTeachingSchedule(
            Teacher_TeachingScheduleRequest teachingScheduleRequest) throws UnsupportedEncodingException, IOException {
        List<Teacher_TeachingScheduleResponse> list = teacherTeachingScheduleExtendRepository
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
