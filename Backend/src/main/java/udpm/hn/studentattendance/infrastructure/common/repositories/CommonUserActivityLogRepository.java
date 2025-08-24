package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.common.model.response.UALResponse;
import udpm.hn.studentattendance.repositories.UserActivityLogRepository;

@Repository
public interface CommonUserActivityLogRepository extends UserActivityLogRepository {

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY ual.created_at DESC ) as orderNumber,
            ual.id,
            ual.created_at,
            ual.updated_at,
            ual.message,
            ual.role,
            f.id AS facilityId,
            f.name AS facilityName,
            COALESCE(ua.id, us.id) AS userId,
            COALESCE(ua.name, us.name) AS userName,
            COALESCE(ua.code, us.code) AS userCode
        FROM user_activity_log ual
        LEFT JOIN facility f ON ual.id_facility = f.id
        LEFT JOIN user_admin ua ON ua.id = ual.id_user
        LEFT JOIN user_staff us ON us.id = ual.id_user
        WHERE 1=1
            AND (COALESCE(:#{#request.facilityId}, '') = '' OR ual.id_facility = :#{#request.facilityId})
            AND (COALESCE(:#{#request.userId}, '') = '' OR ual.id_user = :#{#request.userId})
            AND (:#{#request.role} IS NULL OR ual.role = :#{#request.role})
            AND (COALESCE(TRIM(:#{#request.searchQuery}), '') = '' OR
                 COALESCE(ua.name, us.name, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%') OR
                 COALESCE(ual.message, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%') OR
                 COALESCE(ua.code, us.code, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%'))
            AND (:#{#request.fromDate} IS NULL OR ual.created_at >= :#{#request.fromDate})
            AND (:#{#request.toDate} IS NULL OR ual.created_at <= :#{#request.toDate})
        ORDER BY ual.created_at DESC
    """, countQuery = """
        SELECT COUNT(*)
        FROM user_activity_log ual
        LEFT JOIN facility f ON ual.id_facility = f.id
        LEFT JOIN user_admin ua ON ua.id = ual.id_user
        LEFT JOIN user_staff us ON us.id = ual.id_user
        WHERE 1=1
            AND (COALESCE(:#{#request.facilityId}, '') = '' OR ual.id_facility = :#{#request.facilityId})
            AND (COALESCE(:#{#request.userId}, '') = '' OR ual.id_user = :#{#request.userId})
            AND (:#{#request.role} IS NULL OR ual.role = :#{#request.role})
            AND (COALESCE(TRIM(:#{#request.searchQuery}), '') = '' OR
                 COALESCE(ua.name, us.name, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%') OR
                 COALESCE(ual.message, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%') OR
                 COALESCE(ua.code, us.code, '') LIKE CONCAT('%', TRIM(:#{#request.searchQuery}), '%'))
            AND (:#{#request.fromDate} IS NULL OR ual.created_at >= :#{#request.fromDate})
            AND (:#{#request.toDate} IS NULL OR ual.created_at <= :#{#request.toDate})
    """, nativeQuery = true)
    Page<UALResponse> getListFilter(Pageable pageable, UALFilterRequest request);

}