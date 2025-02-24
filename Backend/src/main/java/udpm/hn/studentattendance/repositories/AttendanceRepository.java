package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
}
