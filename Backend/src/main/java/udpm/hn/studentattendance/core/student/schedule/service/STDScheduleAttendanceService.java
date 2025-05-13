package udpm.hn.studentattendance.core.student.schedule.service;

import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface STDScheduleAttendanceService {

    ResponseObject<?> getList(STDScheduleAttendanceSearchRequest request);
    ByteArrayInputStream exportScheduleAttendance(List<STDScheduleAttendanceResponse> scheduleAttendanceResponses);
}
