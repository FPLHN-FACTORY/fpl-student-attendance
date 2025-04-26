package udpm.hn.studentattendance.core.student.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.schedule.model.request.Student_ScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.repository.Student_ScheduleAttendanceRepository;
import udpm.hn.studentattendance.core.student.schedule.service.Student_ScheduleAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

@Service
@RequiredArgsConstructor
@Validated
public class Student_ScheduleAttendanceServiceImpl implements Student_ScheduleAttendanceService {

    @Autowired
    private Student_ScheduleAttendanceRepository repository;

    @Override
    public ResponseObject<?> getList(Student_ScheduleAttendanceSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getAllListAttendanceByUser(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách điểm danh thành công");
    }
}
