package udpm.hn.studentattendance.core.student.schedule.service;

import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface STDScheduleAttendanceService {

    ResponseObject<?> getList(STDScheduleAttendanceSearchRequest request);
}
