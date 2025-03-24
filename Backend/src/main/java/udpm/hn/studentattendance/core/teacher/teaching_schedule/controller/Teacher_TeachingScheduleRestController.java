package udpm.hn.studentattendance.core.teacher.teaching_schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.service.Teacher_TeachingScheduleService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_TEACHING_SCHEDULE_MANAGEMENT)
public class Teacher_TeachingScheduleRestController {

    private final Teacher_TeachingScheduleService service;

    @GetMapping()
    public ResponseEntity<?> getAllTeachingScheduleByStaff(Teacher_TeachingScheduleRequest request) {
        return service.getAllTeachingScheduleByStaff(request);
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
    public ResponseEntity<?> getDetailPlanDate(@PathVariable("planDateId") String planDateId){
        return service.getDetailPlanDate(planDateId);
    }

    @PutMapping()
    public ResponseEntity<?> updatePlanDate(@RequestBody Teacher_TSPlanDateUpdateRequest planDateUpdateRequest){
        return service.updatePlanDate(planDateUpdateRequest);
    }
}
