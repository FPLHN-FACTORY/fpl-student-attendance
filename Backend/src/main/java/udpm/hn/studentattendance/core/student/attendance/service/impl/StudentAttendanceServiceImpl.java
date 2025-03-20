package udpm.hn.studentattendance.core.student.attendance.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.attendance.model.request.StudentAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.attendance.repository.StudentAttendanceRepository;
import udpm.hn.studentattendance.core.student.attendance.service.StudentAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;


@Service
@RequiredArgsConstructor
@Validated
public class StudentAttendanceServiceImpl implements StudentAttendanceService {


    @Autowired
    private StudentAttendanceRepository repository;

    @Override
    public ResponseObject<?> getList(StudentAttendanceSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getList(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách điểm danh thành công"
        );
    }
}
