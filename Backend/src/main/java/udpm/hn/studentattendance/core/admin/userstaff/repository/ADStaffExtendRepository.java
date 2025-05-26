package udpm.hn.studentattendance.core.admin.userstaff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffDetailResponse;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffResponse;
import udpm.hn.studentattendance.entities.UserStaff;

import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADStaffExtendRepository extends UserStaffRepository {

    @Query(value = """
            SELECT
              ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS orderNumber,
              s.id AS id,
              s.name AS staffName,
              s.code AS staffCode,
              s.email_fe AS staffEmailFe,
              s.email_fpt AS staffEmailFpt,
              s.status AS staffStatus,
              MIN(f.name) AS facilityName,
              f.id AS facilityId,
              GROUP_CONCAT(DISTINCT r.code ORDER BY r.code SEPARATOR ', ') AS roleCode
            FROM user_staff s
              LEFT JOIN role r ON r.id_user_staff = s.id
              LEFT JOIN facility f ON r.id_facility = f.id
            WHERE
              (COALESCE(TRIM(:#{#adStaffRequest.searchQuery}), '') = ''
                OR s.name     LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                OR s.code     LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                OR s.email_fe LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                OR s.email_fpt LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%'))
              AND (COALESCE(:#{#adStaffRequest.idFacility}, '') = '' 
                OR f.id = TRIM(:#{#adStaffRequest.idFacility}))
              AND (:#{#adStaffRequest.status} IS NULL 
                OR s.status = :#{#adStaffRequest.status})
              AND f.status = 1
            GROUP BY
              s.id, s.name, s.code, s.email_fe, s.email_fpt, s.status, s.created_at, f.id
            HAVING
              (:#{#adStaffRequest.roleCodeFilter} IS NULL
               OR SUM(r.code = :#{#adStaffRequest.roleCodeFilter}) > 0)
            ORDER BY
              s.created_at DESC, s.status DESC
            """,
            countQuery = """
                    SELECT COUNT(1)
                    FROM (
                      SELECT s.id
                      FROM user_staff s
                        LEFT JOIN role r ON r.id_user_staff = s.id
                        LEFT JOIN facility f ON r.id_facility = f.id
                      WHERE
                        (COALESCE(TRIM(:#{#adStaffRequest.searchQuery}), '') = ''
                          OR s.name     LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                          OR s.code     LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                          OR s.email_fe LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                          OR s.email_fpt LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%'))
                        AND (COALESCE(:#{#adStaffRequest.idFacility}, '') = '' 
                          OR f.id = TRIM(:#{#adStaffRequest.idFacility}))
                        AND (:#{#adStaffRequest.status} IS NULL 
                          OR s.status = :#{#adStaffRequest.status})
                        AND f.status = 1
                      GROUP BY s.id
                      HAVING (:#{#adStaffRequest.roleCodeFilter} IS NULL
                        OR SUM(r.code = :#{#adStaffRequest.roleCodeFilter}) > 0)
                    ) AS t
                    """,
            nativeQuery = true
    )
    Page<ADStaffResponse> getAllStaff(Pageable pageable, ADStaffRequest adStaffRequest);
    @Query(value = """
            SELECT
              ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS orderNumber,
              s.id AS id,
              s.name AS staffName,
              s.code AS staffCode,
              s.email_fe AS staffEmailFe,
              s.email_fpt AS staffEmailFpt,
              s.status AS staffStatus,
              MIN(f.name) AS facilityName,
              f.id AS facilityId,
              GROUP_CONCAT(DISTINCT r.code ORDER BY r.code SEPARATOR ', ') AS roleCode
            FROM user_staff s
              LEFT JOIN role r ON r.id_user_staff = s.id
              LEFT JOIN facility f ON r.id_facility = f.id
            WHERE
              f.status = 1
            GROUP BY
              s.id, s.name, s.code, s.email_fe, s.email_fpt, s.status, s.created_at, f.id
            ORDER BY
              s.created_at DESC, s.status DESC
            """,
            nativeQuery = true
    )
    List<ADStaffResponse> exportAllStaff();

    Optional<UserStaff> findUserStaffByCode(String staffCode);

    Optional<UserStaff> findUserStaffByEmailFe(String emailFe);

    Optional<UserStaff> findUserStaffByEmailFpt(String emailFpt);

    @Query(value = """
            SELECT
                s.id as id,
                s.code as staffCode,
                s.name as staffName,
                s.email_fe as staffEmailFe,
                s.email_fpt as staffEmailFpt,
                GROUP_CONCAT(DISTINCT r.code ORDER BY r.code SEPARATOR ', ') AS roleCode,
                f.id as facilityId,
                f.name as facilityName
                FROM user_staff AS s
                LEFT JOIN role AS r ON r.id_user_staff = s.id
                LEFT JOIN facility AS f ON f.id = r.id_facility
                WHERE s.id  = :id
                AND f.status = 1
                AND r.status = 1
                GROUP BY s.id, s.code, s.name, s.email_fe, s.email_fpt, f.name, f.id
                        """, nativeQuery = true)
    Optional<ADStaffDetailResponse> getDetailStaff(String id);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_staff
                WHERE code = :newCode
                AND code != :currentCode
            ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistCodeUpdate(String newCode, String currentCode);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_staff us
                WHERE us.email_fe = :newEmailFe
                AND us.email_fe != :currentEmailFe
                    ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistEmailFeUpdate(String newEmailFe, String currentEmailFe);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_staff us
                WHERE us.email_fpt = :newEmailFpt
                AND us.email_fpt != :currentEmailFpt
            ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistEmailFptUpdate(String newEmailFpt, String currentEmailFpt);
}
