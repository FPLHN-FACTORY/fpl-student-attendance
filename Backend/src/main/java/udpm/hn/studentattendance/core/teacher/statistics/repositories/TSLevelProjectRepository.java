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
                COUNT(DISTINCT p.id) AS totalProject
            FROM level_project lp
            LEFT JOIN project p ON
                p.id_level_project = lp.id AND
                EXISTS (
                    SELECT 1
                    FROM semester s
                    JOIN subject_facility sf ON p.id_subject_facility = sf.id
                    JOIN subject s2 ON sf.id_subject = s2.id
                    JOIN factory f ON p.id = f.id_project AND f.status = 1
                    WHERE
                        s.id = p.id_semester AND
                        s.status = 1 AND
                        f.id_user_staff = :idUserStaff AND
                        sf.id_facility = :idFacility AND
                        s.id = :idSemester
                )
            GROUP BY lp.id, lp.name
            ORDER BY lp.name ASC
    """, nativeQuery = true)
    List<TSChartLevelProjectResponse> getStats(String idSemester, String idFacility, String idUserStaff);

}
