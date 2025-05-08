package udpm.hn.studentattendance.core.staff.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.ProjectRepository;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;

@Repository
public interface STProjectSemesterExtendRepository extends SemesterRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                    s.id AS id,
                    s.name AS name,
                    s.code AS code
                FROM semester s
                WHERE s.status = 1
                AND s.to_date > UNIX_TIMESTAMP(CURDATE())
                ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<USSemesterResponse> getSemesters();

    @Query(value = """
                    SELECT 
                    s
                    FROM Semester s
                    WHERE s.status = :status
            """)
    List<Semester> getAllSemester(EntityStatus status);
}
