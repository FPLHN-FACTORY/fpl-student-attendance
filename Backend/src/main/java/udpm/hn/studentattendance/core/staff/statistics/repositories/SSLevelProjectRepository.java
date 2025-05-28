package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSChartLevelProjectResponse;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

import java.util.List;

@Repository
public interface SSLevelProjectRepository extends LevelProjectRepository {

    @Query(value = """
        SELECT
            lp.name AS label,
            COUNT(DISTINCT p.id) AS totalProject
        FROM level_project lp
        LEFT JOIN project p ON p.id_level_project = lp.id AND p.status = 1
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
    List<SSChartLevelProjectResponse> getStats(String idSemester, String idFacility);

}
