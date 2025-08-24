package udpm.hn.studentattendance.core.student.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface STDSFactoryAttendanceBarChartRepository extends FactoryRepository {
    @Query(value = """
    WITH student_shifts AS (
        SELECT DISTINCT pd.id as plan_date_id
        FROM factory f
        JOIN project pro ON f.id_project = pro.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan p ON pf.id_plan = p.id
        JOIN plan_date pd ON pf.id = pd.id_plan_factory
        JOIN user_student_factory usf ON f.id = usf.id_factory
        WHERE 
            usf.id_user_student = :userId AND
            pro.id_semester = :semesterId AND
            pd.status = 1 AND
            pf.status = 1 AND
            pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000
    ),
    attendance_summary AS (
        SELECT 
            ss.plan_date_id,
            a.attendance_status
        FROM student_shifts ss
        LEFT JOIN attendance a ON ss.plan_date_id = a.id_plan_date 
            AND a.id_user_student = :userId
            AND a.status = 1
    )
    SELECT 
        COUNT(*) AS totalShift,
        COUNT(CASE WHEN attendance_status = 3 THEN 1 END) AS totalPresent,
        COUNT(CASE WHEN attendance_status != 3 OR attendance_status IS NULL THEN 1 END) AS totalAbsent
    FROM attendance_summary
    """, nativeQuery = true)
    Optional<STDStatisticsAttendanceChartResponse> getAttendanceBarChart(String userId, String semesterId);


}
