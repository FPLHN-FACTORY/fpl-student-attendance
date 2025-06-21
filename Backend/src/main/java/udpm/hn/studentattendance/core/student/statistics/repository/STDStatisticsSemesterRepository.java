package udpm.hn.studentattendance.core.student.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface STDStatisticsSemesterRepository extends SemesterRepository {

    @Query(value = """
        WITH factory_stats AS (
            SELECT 
                usf.id as factory_id,
                p.id as project_id,
                MIN(pd.start_date) as start_date,
                MAX(pd.end_date) as end_date,
                COUNT(pd.id) as total_sessions,
                COUNT(CASE 
                    WHEN ad.id IS NULL OR ad.attendance_status = 0 
                    THEN 1 
                END) as absent_sessions,
                CASE 
                    WHEN COUNT(pd.id) = 0 THEN 0
                    ELSE (COUNT(CASE WHEN ad.id IS NULL OR ad.attendance_status = 0 THEN 1 END) * 1.0 / COUNT(pd.id))
                END as absent_rate,
                -- Thêm trạng thái để phân biệt các giai đoạn
                CASE 
                    WHEN MIN(pd.start_date) > UNIX_TIMESTAMP(NOW()) * 1000 THEN 'NOT_STARTED'
                    WHEN MIN(pd.start_date) <= UNIX_TIMESTAMP(NOW()) * 1000 
                         AND MAX(pd.end_date) >= UNIX_TIMESTAMP(NOW()) * 1000 THEN 'IN_PROGRESS'
                    ELSE 'COMPLETED'
                END as factory_status
            FROM user_student us 
            INNER JOIN user_student_factory usf ON usf.id_user_student = us.id AND usf.status = 1
            INNER JOIN factory ft ON usf.id_factory = ft.id AND ft.status = 1
            INNER JOIN plan_factory pf ON ft.id = pf.id_factory
            INNER JOIN plan_date pd ON pf.id = pd.id_plan_factory AND pd.status = 1
            INNER JOIN project p ON ft.id_project = p.id AND p.status = 1
            INNER JOIN semester s ON p.id_semester = s.id
            INNER JOIN subject_facility sf ON p.id_subject_facility = sf.id AND sf.status = 1
            LEFT JOIN attendance ad ON us.id = ad.id_user_student AND pd.id = ad.id_plan_date AND ad.status = 1
            WHERE 
                us.id = :idStudent AND
                sf.id_facility = :idFacility AND
                s.id = :idSemester
            GROUP BY usf.id, p.id
        )
        SELECT
            COUNT(DISTINCT factory_id) AS factory,
            COUNT(DISTINCT project_id) AS project,
            COUNT(DISTINCT CASE 
                WHEN factory_status = 'COMPLETED' AND absent_rate <= 0.2
                THEN factory_id 
            END) AS pass,
            COUNT(DISTINCT CASE 
                WHEN factory_status = 'COMPLETED' AND absent_rate > 0.2
                THEN factory_id 
            END) AS fail,
            COUNT(DISTINCT CASE 
                WHEN factory_status = 'IN_PROGRESS' AND absent_rate <= 0.2
                THEN factory_id 
            END) AS process,
            COUNT(DISTINCT CASE
                WHEN factory_status = 'NOT_STARTED' 
                THEN factory_id 
            END) AS notStarted
        FROM factory_stats
        """, nativeQuery = true)
    Optional<STDStatisticsStatResponse> getAllStatisticBySemester(String idFacility, String idStudent, String idSemester);

    List<Semester> getAllSemestersByStatus(EntityStatus status);
}
