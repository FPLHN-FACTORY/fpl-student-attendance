package udpm.hn.studentattendance.core.student.attendance.service;


import udpm.hn.studentattendance.core.admin.project.model.request.AdminProjectSearchRequest;
import udpm.hn.studentattendance.core.student.attendance.model.request.StudentAttendanceSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface StudentAttendanceService {

    ResponseObject<?> getList(StudentAttendanceSearchRequest request);
}
