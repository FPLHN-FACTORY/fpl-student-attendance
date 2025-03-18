package udpm.hn.studentattendance.core.student.history_attendance.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.repository.HistoryAttendanceExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.service.HistoryAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

@Service
@RequiredArgsConstructor
@Validated
public class HistoryAttendanceImpl implements HistoryAttendanceService {
    private final HistoryAttendanceExtendRepository historyAttendanceExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllHistoryAttendanceByStudent(HistoryAttendanceRequest historyAttendanceRequest) {
        Pageable pageable = PaginationHelper.createPageable(historyAttendanceRequest, "createdAt");
        PageableObject list = PageableObject.of(historyAttendanceExtendRepository.getAllFactoryAttendance(pageable, sessionHelper.getUserId(), historyAttendanceRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả lịch sử điểm danh của sinh viên" + sessionHelper.getUserCode() + " thành công",
                        list
                ),
                HttpStatus.OK);
    }
}
