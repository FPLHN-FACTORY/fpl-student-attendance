package udpm.hn.studentattendance.core.student.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface STDSFactoryAttendanceBarChartRepository extends FactoryRepository {
    @Query(value = """
        SELECT
            f.id,
            f.name AS factoryName,
            COUNT(DISTINCT CASE WHEN pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS totalShift,
            (COUNT(DISTINCT CASE WHEN pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) - 
                COALESCE((
                    SELECT COUNT(DISTINCT a.id_plan_date)
                    FROM attendance a
                    JOIN plan_date pd2 ON a.id_plan_date = pd2.id
                    JOIN plan_factory pf2 ON pd2.id_plan_factory = pf2.id
                    WHERE
                        pd2.status = 1 AND
                        pf2.status = 1 AND
                        a.attendance_status = 3 AND
                        pf2.id_factory = f.id AND
                        a.id_user_student = :userId AND
                        pd2.start_date <= UNIX_TIMESTAMP(NOW()) * 1000
                ), 0)
            ) AS totalAbsent
        FROM 
            factory f
            JOIN project pro ON f.id_project = pro.id
            JOIN plan_factory pf ON f.id = pf.id_factory
            JOIN plan p ON pf.id_plan = p.id
            JOIN plan_date pd ON pf.id = pd.id_plan_factory
            JOIN user_student_factory usf ON f.id = usf.id_factory
        WHERE 
            usf.id_user_student = :userId AND
            pro.id_semester = :semesterId AND
            pd.status = 1 AND
            pf.status = 1
        GROUP BY 
            f.id, f.name
        """, nativeQuery = true)
    List<STDStatisticsAttendanceChartResponse> getAttendanceBarChart(String userId, String semesterId);
}
