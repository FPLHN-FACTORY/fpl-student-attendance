package udpm.hn.studentattendance.core.student.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface STDSFactoryAttendanceBarChartRepository extends FactoryRepository {
    @Query(value = """
            WITH cte_shift AS (
                SELECT
                    COUNT(DISTINCT CASE WHEN pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS total_current_shift,
                    COUNT(DISTINCT pd.id) AS total_shift
                FROM 
                factory f 
                JOIN plan_factory pf ON f.id = pf.id_factory
                JOIN plan_date pd ON pf.id = pd.id_plan_factory
                JOIN user_student_factory usf ON f.id = usf.id_factory
                WHERE
                    pd.status = 1 AND
                    pf.status = 1 AND
                    usf.id_user_student = :userId
            )
            SELECT
            DISTINCT 
            f.id,
            f.name AS factoryName,
            cte_s.total_current_shift AS totalShift,
                (cte_s.total_current_shift - (
                        SELECT COUNT(a.id)
                        FROM attendance a
                        JOIN plan_date pd ON a.id_plan_date = pd.id
                        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                        WHERE
                            pd.status = 1 AND
                            pf.status = 1 AND
                            a.attendance_status = 3 AND
                            pf.id_factory = f.id AND
                            a.id_user_student = usf.id_user_student
                    )
                ) AS totalAbsent
            FROM 
            factory f
            JOIN project pro ON f.id_project = pro.id
            JOIN plan_factory pf ON f.id = pf.id_factory
            JOIN plan p ON pf.id_plan = p.id
            JOIN plan_date pd ON pf.id = pd.id_plan_factory
            JOIN user_student_factory usf ON f.id = usf.id_factory
            CROSS JOIN cte_shift cte_s
            WHERE 
            usf.id_user_student = :userId AND
            pro.id_semester = :semesterId
            """, nativeQuery = true)
    List<STDStatisticsAttendanceChartResponse> getAttendanceBarChart(String userId, String semesterId);
}
