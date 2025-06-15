package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.repositories.UserActivityLogRepository;

import java.util.Optional;

@Repository
public interface ADStatisticsRepository extends UserActivityLogRepository {
    @Query(value = """
            SELECT
            (SELECT COUNT(DISTINCT ua.id) FROM user_admin ua WHERE ua.status = 1) AS admin,
            (SELECT COUNT(DISTINCT us.id) FROM user_staff us JOIN role r ON r.id_user_staff = us.id\s
             WHERE us.status = 1 AND r.code = 3) AS teacher,
            (SELECT COUNT(DISTINCT us.id) FROM user_staff us JOIN role r ON r.id_user_staff = us.id\s
             WHERE us.status = 1 AND r.code = 1) AS staff,
            (SELECT COUNT(DISTINCT ual.id) FROM user_activity_log ual\s
             WHERE ual.status = 1\s
             AND (:#{#request.fromDay} IS NULL OR ual.created_at >= :#{#request.fromDay})
             AND (:#{#request.toDay} IS NULL OR ual.created_at <= :#{#request.toDay})
            ) AS logActivity
           \s""", nativeQuery = true)
    Optional<ADStatisticsStatResponse> getAllStatistics(ADStatisticRequest request);
}
