package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;

@Repository
public interface TCSemesterExtendRepository extends SemesterRepository {

    @Query(value = """
                SELECT 
                s
                FROM Semester s 
                WHERE s.status = :status
            """)
    List<Semester> getAllSemester(EntityStatus status);
}
