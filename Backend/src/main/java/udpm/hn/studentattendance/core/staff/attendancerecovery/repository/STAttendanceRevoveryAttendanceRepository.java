package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.List;

@Repository
public interface STAttendanceRevoveryAttendanceRepository extends AttendanceRepository {
    Attendance findByUserStudentIdAndPlanDateId(String userStudentId, String planDateId);

    List<Attendance> findAllByAttendanceRecoveryId(String attendanceRecoveryId);

    @Query(value = """
            SELECT 
            CASE 
            WHEN COUNT(*) > 0 THEN 'TRUE'
            ELSE 'FALSE'
            END
            FROM attendance ad 
            WHERE ad.id_attendance_recovery = :attendanceRecoveryId
            """ , nativeQuery = true)
    boolean isHasStudentRecovery(String attendanceRecoveryId);
}
