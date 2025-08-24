package udpm.hn.studentattendance.core.student.schedule.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface STDScheduleAttendanceService {

    ResponseEntity<?> getList(STDScheduleAttendanceSearchRequest request);
    ByteArrayInputStream exportScheduleAttendance(List<STDScheduleAttendanceResponse> scheduleAttendanceResponses);
}
