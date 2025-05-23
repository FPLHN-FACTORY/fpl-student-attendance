package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.List;

@Repository
public interface USFactoryAttendanceExtendRepository extends AttendanceRepository {

    List<Attendance> getAttendanceByPlanDateId(String planDateId);

    @Query(value = """
            DELETE attendance WHERE id_plan_date = :planDateId
            """, nativeQuery = true
    )
    void deleteAllAttendanceByPlanDate(String planDateId);
}
