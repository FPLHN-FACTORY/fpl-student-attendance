package udpm.hn.studentattendance.core.teacher.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartLevelProjectResponse;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

import java.util.List;

@Repository
public interface TSLevelProjectRepository extends LevelProjectRepository {

    @Query(value = """
        SELECT
            lp.name AS label,
            COUNT(DISTINCT CASE WHEN f.id_user_staff = :idUserStaff THEN p.id END) AS totalProject
        FROM level_project lp
        LEFT JOIN project p ON p.id_level_project = lp.id AND p.status = 1
        LEFT JOIN factory f ON p.id = f.id_project AND f.status = 1
        WHERE
            lp.status = 1 AND
            p.id IS NULL OR EXISTS(
                SELECT 1
                FROM semester s
                JOIN subject_facility sf ON p.id_subject_facility = sf.id
                JOIN subject s2 ON sf.id_subject = s2.id
                WHERE
                     s.id = p.id_semester AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     sf.status = 1 AND
                     sf.id_facility = :idFacility AND
                     s.id = :idSemester
            )
        GROUP BY lp.id, lp.name
        ORDER BY lp.name ASC
    """, nativeQuery = true)
    List<TSChartLevelProjectResponse> getStats(String idSemester, String idFacility, String idUserStaff);

}
