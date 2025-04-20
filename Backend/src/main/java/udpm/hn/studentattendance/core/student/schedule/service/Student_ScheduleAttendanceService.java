package udpm.hn.studentattendance.core.student.schedule.service;


import udpm.hn.studentattendance.core.student.schedule.model.request.Student_ScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Student_ScheduleAttendanceService {

    ResponseObject<?> getList(Student_ScheduleAttendanceSearchRequest request);
}
