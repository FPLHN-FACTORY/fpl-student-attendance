package udpm.hn.studentattendance.core.student.history_attendance.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.Student_HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceFactoryExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceSemesterExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.service.Student_HistoryAttendanceService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class Student_HistoryAttendanceImpl implements Student_HistoryAttendanceService {
    private final Student_HistoryAttendanceExtendRepository historyAttendanceExtendRepository;

    private final SessionHelper sessionHelper;

    private final Student_HistoryAttendanceSemesterExtendRepository historyAttendanceSemesterExtendRepository;

    private final Student_HistoryAttendanceFactoryExtendRepository historyAttendanceFactoryExtendRepository;

    @Override
    public ResponseEntity<?> getAllHistoryAttendanceByStudent(Student_HistoryAttendanceRequest historyAttendanceRequest) {
        Pageable pageable = PaginationHelper.createPageable(historyAttendanceRequest, "createdAt");
        PageableObject list = PageableObject.of(historyAttendanceExtendRepository.getAllFactoryAttendance(sessionHelper.getCurrentUser().getId(), pageable, historyAttendanceRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả lịch sử điểm danh của sinh viên " + sessionHelper.getCurrentUser().getCode() + " thành công",
                        list
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = historyAttendanceSemesterExtendRepository.getAllSemesterByCode(EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả học kỳ thành công",
                        semesters
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllFactoryByUserStudent() {
        List<Factory> factories = historyAttendanceFactoryExtendRepository.getAllFactoryByUser(EntityStatus.ACTIVE, sessionHelper.getUserId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả nhóm xưởng của sinh viên " + sessionHelper.getUserCode() + " thành công",
                        factories
                ),
                HttpStatus.OK);
    }
}
