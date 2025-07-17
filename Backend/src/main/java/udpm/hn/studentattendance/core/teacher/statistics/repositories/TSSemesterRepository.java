package udpm.hn.studentattendance.core.teacher.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSAllStatsResponse;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.Optional;

@Repository
public interface TSSemesterRepository extends SemesterRepository {

    @Query(value = """
        WITH cte_factory AS (
            SELECT
                f.id,
                LEAST(f.status, p.status, s.status, sf.status, s2.status, pf.status, pl.status) AS status,
                COUNT(DISTINCT pd.id) AS total_shift,
                COUNT(DISTINCT CASE WHEN pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS total_process_shift
            FROM factory f
            JOIN project p ON f.id_project = p.id
            JOIN semester s ON p.id_semester = s.id
            JOIN subject_facility sf ON p.id_subject_facility = sf.id
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN plan_factory pf ON pf.id_factory = f.id
            JOIN plan pl ON p.id = pl.id_project
            JOIN plan_date pd ON pd.id_plan_factory = pf.id
            WHERE
                s.status = 1 AND
                pd.status = 1 AND
                f.id_user_staff = :idUserStaff AND
                s.id = :idSemester AND
                sf.id_facility = :idFacility
            GROUP BY f.id, f.status, p.status, s.status, sf.status, s2.status, pf.status, pl.status
        ),
        cte_plan AS (
            SELECT
                SUM(CASE WHEN total_shift = total_process_shift AND total_shift > 0 THEN 1 ELSE 0 END) AS totalPlanComplete,
                SUM(CASE WHEN status <> 1 THEN 1 ELSE 0 END) AS totalPlanCancel,
                SUM(CASE WHEN total_shift > total_process_shift THEN 1 ELSE 0 END) AS totalPlanProcess
            FROM cte_factory
        )
        SELECT
            COUNT(DISTINCT pd.id) AS totalShiftToday,
            COUNT(DISTINCT p.id) AS totalProject,
            COUNT(DISTINCT f.id) AS totalFactory,
            COUNT(DISTINCT ust.id) AS totalStudent,
            (SELECT totalPlanComplete FROM cte_plan) AS totalPlanComplete,
            (SELECT totalPlanProcess FROM cte_plan) AS totalPlanProcess,
            (SELECT totalPlanCancel FROM cte_plan) AS totalPlanCancel
        FROM semester s
        JOIN project p ON s.id = p.id_semester
        JOIN subject_facility sf ON p.id_subject_facility = sf.id
        JOIN subject s2 ON sf.id_subject = s2.id
        JOIN factory f ON p.id = f.id_project AND f.status = 1
        LEFT JOIN user_student_factory usf ON f.id = usf.id_factory AND usf.status = 1
        LEFT JOIN user_student ust ON usf.id_user_student = ust.id AND ust.status = 1
        LEFT JOIN plan_factory pf ON f.id = pf.id_factory AND pf.status = 1
        LEFT JOIN plan_date pd ON pf.id = pd.id_plan_factory AND pd.status = 1 AND DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE()
        WHERE
            s.status = 1 AND
            f.id_user_staff = :idUserStaff AND
            sf.id_facility = :idFacility AND
            s.id = :idSemester
    """, nativeQuery = true)
    Optional<TSAllStatsResponse> getAllStats(String idSemester, String idFacility, String idUserStaff);

}
