package udpm.hn.studentattendance.core.admin.user_staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.Admin_StaffRequest;
import udpm.hn.studentattendance.core.admin.user_staff.model.response.Admin_StaffDetailResponse;
import udpm.hn.studentattendance.core.admin.user_staff.model.response.Admin_StaffResponse;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;

@Repository
public interface Admin_StaffRepository extends UserStaffRepository {

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
                 LEFT JOIN role r on r.id_user_staff = s.id
                 LEFT JOIN facility f on r.id_facility = f.id
            WHERE (trim(:#{#adStaffRequest.searchQuery}) IS NULL 
                   OR trim(:#{#adStaffRequest.searchQuery}) = '' 
                   OR s.name LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.code LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.email_fe LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.email_fpt LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%'))
              AND (trim(:#{#adStaffRequest.idFacility}) IS NULL 
                   OR trim(:#{#adStaffRequest.idFacility}) = '' 
                   OR f.id = trim(:#{#adStaffRequest.idFacility}))
              AND (:#{#adStaffRequest.status} IS NULL OR s.status = :#{#adStaffRequest.status})
            GROUP BY s.id, s.name, s.code, s.email_fe, s.email_fpt, s.status, s.created_at, f.id
            ORDER BY s.created_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.id)
                    FROM user_staff s
                         LEFT JOIN role r on r.id_user_staff = s.id
                         LEFT JOIN facility f on r.id_facility = f.id
                    WHERE (trim(:#{#adStaffRequest.searchQuery}) IS NULL 
                           OR trim(:#{#adStaffRequest.searchQuery}) = '' 
                           OR s.name LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.code LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.email_fe LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.email_fpt LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%'))
                    AND (trim(:#{#adStaffRequest.idFacility}) IS NULL 
                           OR trim(:#{#adStaffRequest.idFacility}) = '' 
                           OR f.id = trim(:#{#adStaffRequest.idFacility}))
                    AND (:#{#adStaffRequest.status} IS NULL OR s.status = :#{#adStaffRequest.status})
                            """, nativeQuery = true)
    Page<Admin_StaffResponse> getAllStaff(Pageable pageable, Admin_StaffRequest adStaffRequest);


    Optional<UserStaff> findUserStaffByCode(String staffCode);

    Optional<UserStaff> findUserStaffByEmailFe(String emailFe);

    Optional<UserStaff> findUserStaffByEmailFpt(String emailFpt);

    @Query(
            value =
                    """
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
    Optional<Admin_StaffDetailResponse> getDetailStaff(String id);

    @Query(
            value = """
                    SELECT CASE WHEN EXISTS (
                        SELECT 1
                        FROM user_staff
                        WHERE code = :newCode
                        AND code != :currentCode
                    ) THEN 'TRUE' ELSE 'FALSE' END
                    """, nativeQuery = true
    )
    boolean isExistCodeUpdate(String newCode, String currentCode);

    @Query(
            value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_staff us
                WHERE us.email_fe = :newEmailFe
                AND us.email_fe != :currentEmailFe
                    ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true
    )
    boolean isExistEmailFeUpdate(String newEmailFe, String currentEmailFe);

    @Query(
            value = """
                    SELECT CASE WHEN EXISTS (
                        SELECT 1
                        FROM user_staff us
                        WHERE us.email_fpt = :newEmailFpt
                        AND us.email_fpt != :currentEmailFpt
                    ) THEN 'TRUE' ELSE 'FALSE' END
                    """, nativeQuery = true
    )
    boolean isExistEmailFptUpdate(String newEmailFpt, String currentEmailFpt);
}
