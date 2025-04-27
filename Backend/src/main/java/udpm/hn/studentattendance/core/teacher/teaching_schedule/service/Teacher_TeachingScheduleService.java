package udpm.hn.studentattendance.core.teacher.teaching_schedule.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TeachingScheduleResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface Teacher_TeachingScheduleService {
    ResponseEntity<?> getAllTeachingScheduleByStaff(Teacher_TeachingScheduleRequest teachingScheduleRequest);

    ResponseEntity<?> getAllFactoryByStaff();

    ResponseEntity<?> getAllProjectByStaff();

    ResponseEntity<?> getAllSubjectByStaff();

    ResponseEntity<?> getAllType();

    ByteArrayInputStream exportTeachingSchedule(List<Teacher_TeachingScheduleResponse> teachingScheduleResponseList);

    ResponseEntity<?> updatePlanDate(Teacher_TSPlanDateUpdateRequest planDateUpdateRequest);

    ResponseEntity<?> getDetailPlanDate(String planDateId);

    ResponseEntity<?> getAllTeachingSchedulePresent(Teacher_TeachingScheduleRequest teachingScheduleRequest);

    ResponseEntity<?> changeTypePlanDate(String planDateId);
}
