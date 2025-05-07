package udpm.hn.studentattendance.core.teacher.studentattendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.response.TeacherStudentAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.studentattendance.repository.TeacherStudentAttendanceRepository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.repositories.PlanDateRepository;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherStudentAttendanceServiceImpl implements TeacherStudentAttendanceService {

    @Autowired
    private TeacherStudentAttendanceRepository repository;
    @Autowired
    private UserStudentRepository userStudentRepository;
    @Autowired
    private PlanDateRepository planDateRepository;

    @Override
    public ResponseEntity<?> createAttendance(String planDateId) {
        // Lấy danh sách học sinh cho planDate (nếu cần bulk tạo)
        List<String> studentIds = repository.getUserStudentIdsByPlanDate(planDateId);
        if (studentIds.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(
                    RestApiStatus.ERROR,
                    "Không tìm thấy sinh viên cho ngày điểm danh",
                    null), HttpStatus.BAD_REQUEST);
        }
        List<Attendance> results = new ArrayList<>();
        for (String studentId : studentIds) {
            Optional<String> optAttId = repository.findAttendanceIdByPlanDateAndStudent(planDateId, studentId);
            Attendance attendance;
            if (optAttId.isPresent()) {
                attendance = repository.findById(optAttId.get()).orElseThrow();
            } else {
                attendance = new Attendance();
                attendance.setUserStudent(userStudentRepository.findById(studentId).orElseThrow());
                attendance.setPlanDate(planDateRepository.findById(planDateId).orElseThrow());
            }
            attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
            results.add(repository.save(attendance));
        }
        return new ResponseEntity<>(new ApiResponse(
                RestApiStatus.SUCCESS,
                "Bulk điểm danh sinh viên thành công",
                results), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getAllByPlanDate(String planDateId) {
        List<TeacherStudentAttendanceResponse> list = repository.getAllByPlanDate(planDateId);
        return new ResponseEntity<>(new ApiResponse(
                RestApiStatus.SUCCESS,
                "Lấy danh sách sinh viên điểm danh thành công",
                list), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateStatusAttendance(TeacherStudentAttendanceRequest req) {
        Attendance attendance;
        // Nếu đã có attendanceId từ client -> chỉ update
        if (req.getId() != null) {
            attendance = repository.findById(req.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Attendance không tồn tại"));
        } else if ("3".equals(req.getStatus())) {
            // Nếu chưa có record và user đánh dấu Có mặt -> tạo mới
            attendance = new Attendance();
            attendance.setUserStudent(
                    userStudentRepository.findById(req.getUserStudentId()).orElseThrow());
            attendance.setPlanDate(
                    planDateRepository.findById(req.getPlanDateId()).orElseThrow());
        } else {
            // Chưa có record và đánh dấu Vắng mặt -> không cần lưu
            return new ResponseEntity<>(new ApiResponse(
                    RestApiStatus.SUCCESS,
                    "Không có thay đổi",
                    null), HttpStatus.OK);
        }
        // Cập nhật trạng thái
        if ("3".equals(req.getStatus())) {
            attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        } else {
            attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
        }
        Attendance saved = repository.save(attendance);
        return new ResponseEntity<>(new ApiResponse(
                RestApiStatus.SUCCESS,
                "Cập nhật trạng thái điểm danh thành công",
                saved), HttpStatus.OK);
    }
}
