package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

@Repository
public interface STAttendanceRevoveryAttendanceRepository extends AttendanceRepository {
    Attendance findByUserStudentIdAndPlanDateId(String userStudentId, String planDateId);

}
