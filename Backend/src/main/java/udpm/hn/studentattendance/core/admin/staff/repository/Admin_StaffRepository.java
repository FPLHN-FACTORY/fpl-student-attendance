package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.Admin_StaffDetailResponse;
import udpm.hn.studentattendance.core.admin.staff.model.response.Admin_StaffResponse;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;

@Repository
public interface Admin_StaffRepository extends UserStaffRepository {

    @Query(value = """
            SELECT 
                 ROW_NUMBER() OVER (ORDER BY s.createdAt DESC) AS rowNumber,
                 s.id AS id,
                 s.name AS staffName,
                 s.code AS staffCode,
                 s.emailFe AS staffEmailFe,
                 s.emailFpt AS staffEmailFpt,
                 s.status AS staffStatus,
                 min(f.name) AS facilityName
            FROM UserStaff s
                 LEFT JOIN Role r on r.userStaff.id = s.id
                 LEFT JOIN Facility  f on r.facility.id = f.id
            WHERE (trim(:#{#adStaffRequest.searchQuery}) IS NULL 
                   OR trim(:#{#adStaffRequest.searchQuery}) = '' 
                   OR s.name LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.code LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.emailFe LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                   OR s.emailFpt LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%'))
              AND (trim(:#{#adStaffRequest.idFacility}) IS NULL 
                   OR trim(:#{#adStaffRequest.idFacility}) = '' 
                   OR f.id = trim(:#{#adStaffRequest.idFacility}))
              AND (:#{#adStaffRequest.status} IS NULL OR s.status = :#{#adStaffRequest.status})
            GROUP BY s.id, s.name, s.code, s.emailFe, s.emailFpt, s.status, s.createdAt
            ORDER BY s.createdAt DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.id)
                    FROM UserStaff s
                         LEFT JOIN Role r on r.userStaff.id = s.id
                         LEFT JOIN Facility  f on r.facility.id = f.id
                    WHERE (trim(:#{#adStaffRequest.searchQuery}) IS NULL 
                           OR trim(:#{#adStaffRequest.searchQuery}) = '' 
                           OR s.name LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.code LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.emailFe LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%')
                           OR s.emailFpt LIKE concat('%', trim(:#{#adStaffRequest.searchQuery}), '%'))
                      AND (trim(:#{#adStaffRequest.idFacility}) IS NULL 
                           OR trim(:#{#adStaffRequest.idFacility}) = '' 
                           OR f.id = trim(:#{#adStaffRequest.idFacility}))
                      AND (:#{#adStaffRequest.status} IS NULL OR s.status = :#{#adStaffRequest.status})
                    """)
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
                                   s.emailFe as staffEmailFe,
                                   s.emailFpt as staffEmailFpt
                                   FROM UserStaff AS s
                                   WHERE s.id  = :id
                            """)
    Optional<Admin_StaffDetailResponse> getDetailStaff(String id);

    Optional<UserStaff> findUserStaffByIdAndStatus(String staffId, EntityStatus status);
}
