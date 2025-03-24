package udpm.hn.studentattendance.core.teacher.teaching_schedule.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;

public interface Teacher_TeachingScheduleService {
    ResponseEntity<?> getAllTeachingScheduleByStaff(Teacher_TeachingScheduleRequest teachingScheduleRequest);

    ResponseEntity<?> getAllFactoryByStaff();

    ResponseEntity<?> getAllProjectByStaff();

    ResponseEntity<?> getAllSubjectByStaff();

    ResponseEntity<?> getAllShift();

    ResponseEntity<?> updatePlanDate(Teacher_TSPlanDateUpdateRequest planDateUpdateRequest);

    ResponseEntity<?> getDetailPlanDate(String planDateId);
}
