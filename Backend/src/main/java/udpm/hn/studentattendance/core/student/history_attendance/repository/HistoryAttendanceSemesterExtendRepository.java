package udpm.hn.studentattendance.core.student.history_attendance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import javax.swing.text.html.parser.Entity;
import java.util.List;

@Repository
public interface HistoryAttendanceSemesterExtendRepository extends SemesterRepository {
    @Query(
            value =
                    """
                    SELECT 
                    s
                    FROM 
                    Semester s
                    WHERE 
                    s.status = :semesterStatus
                            """
    )
    List<Semester> getAllSemesterByCode(EntityStatus semesterStatus);
}
