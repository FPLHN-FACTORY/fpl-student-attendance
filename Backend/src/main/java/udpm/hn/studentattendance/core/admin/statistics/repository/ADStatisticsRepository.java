package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;
import udpm.hn.studentattendance.repositories.UserActivityLogRepository;

import java.util.Optional;

@Repository
public interface ADStatisticsRepository extends UserActivityLogRepository {
    @Query(value = """
            SELECT
            (SELECT COUNT(DISTINCT ua.id) FROM user_admin ua WHERE ua.status = 1) AS admin,
            (SELECT COUNT(DISTINCT us.id) FROM user_staff us JOIN role r ON r.id_user_staff = us.id
             WHERE us.status = 1 AND r.code = 3) AS teacher,
            (SELECT COUNT(DISTINCT us.id) FROM user_staff us JOIN role r ON r.id_user_staff = us.id
             WHERE us.status = 1 AND r.code = 1) AS staff,
            (SELECT COUNT(DISTINCT sb.id) FROM subject sb WHERE sb.status = 1) AS subject,
            (SELECT COUNT(DISTINCT p.id) 
            FROM project p JOIN semester s ON p.id_semester = s.id
            WHERE
            s.status = 1 AND
            p.status = 1) AS totalProject
           """, nativeQuery = true)
    Optional<ADStatisticsStatResponse> getAllStatistics();

    @Query(value = """
                    SELECT 
                    (SELECT COUNT(DISTINCT sb.id) FROM subject sb WHERE sb.status = 1)AS totalSubject,
                    (SELECT COUNT(DISTINCT p.id) FROM project p WHERE p.status = 1)AS totalProject
                        """, nativeQuery = true)
    Optional<ADSTotalProjectAndSubjectResponse> getTotalProjectAndSubject();
}
