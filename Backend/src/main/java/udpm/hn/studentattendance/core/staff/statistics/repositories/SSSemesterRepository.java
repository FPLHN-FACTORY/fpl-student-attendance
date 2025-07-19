package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSAllStatsResponse;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.Optional;

@Repository
public interface SSSemesterRepository extends SemesterRepository {

    @Query(value = """
        WITH cte_factory AS (
            SELECT
                f.id,
                COUNT(DISTINCT CASE WHEN pf.status = 1 THEN pd.id END) AS total_shift,
                COUNT(DISTINCT CASE WHEN pf.status = 1 AND pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS total_process_shift
            FROM factory f
            JOIN project p ON f.id_project = p.id
            JOIN semester s ON p.id_semester = s.id
            JOIN subject_facility sf ON p.id_subject_facility = sf.id
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN plan_factory pf ON pf.id_factory = f.id
            JOIN plan pl ON p.id = pl.id_project
            JOIN plan_date pd ON pd.id_plan_factory = pf.id
            WHERE
                f.status = 1 AND
                s.status = 1 AND
                s2.status = 1 AND
                sf.status = 1 AND
                pl.status = 1 AND
                p.status = 1 AND
                pd.status = 1 AND
                s.id = :idSemester AND
                sf.id_facility = :idFacility
            GROUP BY f.id
        ),
        cte_plan AS (
            SELECT
                SUM(CASE WHEN total_shift = total_process_shift AND total_shift > 0 THEN 1 ELSE 0 END) AS totalPlanComplete,
                SUM(CASE WHEN total_shift > total_process_shift THEN 1 ELSE 0 END) AS totalPlanProcess
            FROM cte_factory
        )
        SELECT
            COUNT(DISTINCT p.id) AS totalProject,
            COUNT(DISTINCT f.id) AS totalFactory,
            COUNT(DISTINCT us.id) AS totalTeacher,
            COUNT(DISTINCT ust.id) AS totalStudent,
            COUNT(DISTINCT pl.id) AS totalPlan,
            (SELECT totalPlanComplete FROM cte_plan) AS totalPlanComplete,
            (SELECT totalPlanProcess FROM cte_plan) AS totalPlanProcess,
            COUNT(DISTINCT CASE WHEN pl.status = 0 THEN pl.id END) AS totalPlanCancel
        FROM semester s
        JOIN project p ON s.id = p.id_semester
        JOIN subject_facility sf ON p.id_subject_facility = sf.id
        JOIN subject s2 ON sf.id_subject = s2.id
        LEFT JOIN factory f ON p.id = f.id_project AND f.status = 1
        LEFT JOIN user_staff us ON f.id_user_staff = us.id AND us.status = 1
        LEFT JOIN user_student_factory usf ON f.id = usf.id_factory AND usf.status = 1
        LEFT JOIN user_student ust ON usf.id_user_student = ust.id AND ust.status = 1
        LEFT JOIN plan pl ON p.id = pl.id_project
        WHERE
            s.status = 1 AND
            s2.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            sf.id_facility = :idFacility AND
            s.id = :idSemester
    """, nativeQuery = true)
    Optional<SSAllStatsResponse> getAllStats(String idSemester, String idFacility);

}
