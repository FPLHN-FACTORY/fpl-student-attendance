package udpm.hn.studentattendance.core.student.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface STDFactoryLineChartRepository extends FactoryRepository {
    @Query(value = """
        SELECT
        ft.name AS factoryName,
        COALESCE(
            ROUND(
                (COUNT(CASE WHEN ad.attendance_status = 3 THEN 1 END) * 100.0 / COUNT(CASE WHEN pd.status = 1 THEN 1 END)), 2
            ), 0
        ) AS attendancePercentage
        FROM
        user_student us
        LEFT JOIN user_student_factory usf ON us.id = usf.id_user_student
        LEFT JOIN factory ft ON usf.id_factory = ft.id
        LEFT JOIN plan_factory pf ON ft.id = pf.id_factory
        LEFT JOIN plan_date pd ON pf.id = pd.id_plan_factory
        LEFT JOIN attendance ad ON pd.id = ad.id_plan_date AND ad.id_user_student = us.id AND ad.status = 1
        LEFT JOIN project p ON ft.id_project = p.id
        LEFT JOIN semester s ON p.id_semester = s.id
        LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
        WHERE 
        us.id = :idStudent AND
        s.id = :idSemester AND
        sf.id_facility = :idFacility AND
        us.status = 1 AND 
        usf.status = 1 AND
        ft.status = 1 AND 
        pf.status = 1 AND
        pd.status = 1
        GROUP BY ft.id, ft.name
        ORDER BY ft.name
        """, nativeQuery = true)
    List<STDStatisticsFactoryChartResponse> getAttendancePercentage(String idFacility, String idStudent, String idSemester);
}
