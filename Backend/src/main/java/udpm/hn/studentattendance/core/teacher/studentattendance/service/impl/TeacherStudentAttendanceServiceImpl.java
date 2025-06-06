package udpm.hn.studentattendance.core.teacher.studentattendance.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.response.TeacherStudentAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.studentattendance.repository.TeacherStudentAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.studentattendance.service.TeacherStudentAttendanceService;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.repositories.PlanDateRepository;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class TeacherStudentAttendanceServiceImpl implements TeacherStudentAttendanceService {

    private final TeacherStudentAttendanceRepository repository;

    private final UserStudentRepository userStudentRepository;

    private final PlanDateRepository planDateRepository;

    private final SessionHelper sessionHelper;

    private final UserActivityLogHelper userActivityLogHelper;

    @Override
    public ResponseEntity<?> createAttendance(String planDateId) {
        // Lấy danh sách học sinh cho planDate (nếu cần bulk tạo)
        List<String> studentIds = repository.getUserStudentIdsByPlanDate(planDateId);
        if (studentIds.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy sinh viên cho ngày điểm danh");
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
        return RouterHelper.responseSuccess("Tạo điểm danh sinh viên thành công", results);
    }

    @Override
    public ResponseEntity<?> getAllByPlanDate(String planDateId) {
        List<TeacherStudentAttendanceResponse> list = repository.getAllByPlanDate(planDateId,
                sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy tất cả sinh viên nhóm xưởng", list);
    }

    @Override
    public ResponseEntity<?> updateStatusAttendance(TeacherModifyStudentAttendanceRequest request) {

        List<TeacherStudentAttendanceRequest> students = request.getStudents();

        List<Attendance> lstData = new ArrayList<>();

        for (TeacherStudentAttendanceRequest req : students) {

            if (req.getIdUserStudent() == null || req.getIdPlanDate() == null) {
                continue;
            }

            PlanDate planDate = planDateRepository.findById(req.getIdPlanDate()).orElse(null);
            UserStudent userStudent = userStudentRepository.findById(req.getIdUserStudent()).orElse(null);

            if (planDate == null
                    || userStudent == null
                    || !planDate.getPlanFactory().getFactory().getUserStaff().getId()
                            .equals(sessionHelper.getUserId())) {
                continue;
            }

            Attendance attendance = null;

            if (req.getIdAttendance() != null) {
                attendance = repository.findById(req.getIdAttendance()).orElse(null);
            }

            if (req.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
                if (attendance == null) {
                    attendance = new Attendance();
                    attendance.setUserStudent(userStudent);
                    attendance.setPlanDate(planDate);
                }
                attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
            } else {
                if (attendance != null) {
                    attendance.setUpdatedAt(attendance.getUpdatedAt());
                    attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
                }
            }

            if (attendance != null) {
                lstData.add(attendance);
            }
        }        if (lstData.isEmpty()) {
            return RouterHelper.responseError("Không có thay đổi nào");
        }

        repository.saveAllAndFlush(lstData);
        
        // Log the activity
        int presentCount = 0;
        int absentCount = 0;
        for (Attendance attendance : lstData) {
            if (attendance.getAttendanceStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            }
        }
        
        String logMessage = "vừa cập nhật trạng thái điểm danh cho " + lstData.size() + " sinh viên";
        if (presentCount > 0 && absentCount > 0) {
            logMessage += " (" + presentCount + " có mặt, " + absentCount + " vắng mặt)";
        } else if (presentCount > 0) {
            logMessage += " (tất cả có mặt)";
        } else if (absentCount > 0) {
            logMessage += " (tất cả vắng mặt)";
        }
        
        userActivityLogHelper.saveLog(logMessage);
        return RouterHelper.responseSuccess("Cập nhật trạng thái điểm danh thành công");
    }
}
