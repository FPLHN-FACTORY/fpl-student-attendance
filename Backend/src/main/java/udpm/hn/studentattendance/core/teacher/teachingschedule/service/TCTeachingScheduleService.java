package udpm.hn.studentattendance.core.teacher.teachingschedule.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface TCTeachingScheduleService {
    ResponseEntity<?> getAllTeachingScheduleByStaff(TCTeachingScheduleRequest teachingScheduleRequest);

    ResponseEntity<?> getAllFactoryByStaff();

    ResponseEntity<?> getAllProjectByStaff();

    ResponseEntity<?> getAllSubjectByStaff();

    ResponseEntity<?> getAllType();

    ByteArrayInputStream exportTeachingSchedule(List<TCTeachingScheduleResponse> teachingScheduleResponseList);

    ResponseEntity<?> updatePlanDate(TCTSPlanDateUpdateRequest planDateUpdateRequest);

    ResponseEntity<?> getDetailPlanDate(String planDateId);

    ResponseEntity<?> getAllTeachingSchedulePresent(TCTeachingScheduleRequest teachingScheduleRequest);

    ResponseEntity<?> changeTypePlanDate(String planDateId);
}
