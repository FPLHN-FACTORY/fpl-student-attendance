package udpm.hn.studentattendance.core.teacher.studentattendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.service.TeacherStudentAttendanceService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STUDENT_ATTENDANCE)
public class TeacherStudentAttendanceRestController {

    private final TeacherStudentAttendanceService service;

    /**
     * Bulk tạo hoặc cập nhật điểm danh mặc định (PRESENT) cho toàn bộ sinh viên của planDate
     */
    @PostMapping("{planDateId}")
    public ResponseEntity<?> createBulk(@PathVariable("planDateId") String planDateId) {
        return service.createAttendance(planDateId);
    }

    /**
     * Lấy danh sách sinh viên và trạng thái điểm danh cho planDate
     */
    @GetMapping("/show/{planDateId}")
    public ResponseEntity<?> show(@PathVariable("planDateId") String planDateId) {
        return service.getAllByPlanDate(planDateId);
    }

    /**
     * Cập nhật trạng thái điểm danh cho một bản ghi
     */
    @PutMapping()
    public ResponseEntity<?> updateStatus(@RequestBody TeacherModifyStudentAttendanceRequest req) {
        return service.updateStatusAttendance(req);
    }
}
