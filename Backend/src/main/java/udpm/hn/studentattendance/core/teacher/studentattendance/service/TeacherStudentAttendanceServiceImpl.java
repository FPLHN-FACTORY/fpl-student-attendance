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
    public ResponseEntity<?> createAttendance(String request) {
        List<String> getIdStudentByPlanDate = repository.getIdStudentByIdPlanDate(request);
        for (String id : getIdStudentByPlanDate) {
            String check = repository.getIdAttendanceByIdStudentAndPlanDate(id, request);
            if (check == null) {
                Attendance attendance = new Attendance();
                attendance.setUserStudent(userStudentRepository.findById(id).get());
                attendance.setPlanDate(planDateRepository.findById(request).get());
                attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
                repository.save(attendance);
            }
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Tạo điểm danh thành công",
                        null),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllByPlanDate(String req) {
        List<TeacherStudentAttendanceResponse> list = repository.getAll(req);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy danh sách sinh viên điểm danh thành công",
                        list),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateStatusAttendance(TeacherStudentAttendanceRequest req) {
        Attendance attendance = repository.findById(req.getId()).get();
        if (req.getStatus().equals("3")) {
            attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        } else {
            attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
        }
        repository.save(attendance);

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật thành coong",
                        attendance),
                HttpStatus.OK);
    }
}
