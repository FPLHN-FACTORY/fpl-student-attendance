package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.respone.AdStaffDetailRespone;
import udpm.hn.studentattendance.core.admin.staff.model.respone.AdStaffRespone;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;
import java.util.OptionalInt;

@Repository
public interface AdStaffRepository extends UserStaffRepository {

    @Query(
            value =
                    """
                            SELECT\s
                                s.id as staffId,
                                s.name as staffName,
                                s.code as staffCode,
                                s.emailFe as staffEmailFe,
                                s.emailFpt as staffEmailFpt,
                                s.status as staffStatus
                            FROM UserStaff as s
                            LEFT JOIN Role as r on s.id = r.userStaff.id
                            LEFT JOIN Facility as f on r.facility.id = f.id
                            WHERE\s
                                (TRIM(:#{#adStaffRequest.searchQuery}) IS NULL
                                 OR TRIM(:#{#adStaffRequest.searchQuery}) = ''
                                 OR s.name LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                                 OR s.code LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                                 OR s.emailFe LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                                 OR s.emailFpt LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%'))
                              AND\s
                                (TRIM(:#{#adStaffRequest.idFacility}) IS NULL
                                 OR TRIM(:#{#adStaffRequest.idFacility}) = ''
                                 OR f.id = TRIM(:#{#adStaffRequest.idFacility}))
                              AND\s
                                (TRIM(:#{#adStaffRequest.status}) IS NULL
                                 OR TRIM(:#{#adStaffRequest.status}) = ''
                                 OR s.status = TRIM(:#{#adStaffRequest.status}))
                            GROUP BY s.id, s.name, s.code, s.emailFe, s.emailFpt, s.status
                            ORDER BY s.createdAt DESC
                                     """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.id) AS totalCount
                    FROM UserStaff AS s
                    LEFT JOIN Role AS r ON s.id = r.userStaff.id
                    LEFT JOIN Facility AS f ON r.facility.id = f.id
                    WHERE\s
                        (TRIM(:#{#adStaffRequest.searchQuery}) IS NULL
                         OR TRIM(:#{#adStaffRequest.searchQuery}) = ''
                         OR s.name LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                         OR s.code LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                         OR s.emailFe LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%')
                         OR s.emailFpt LIKE CONCAT('%', TRIM(:#{#adStaffRequest.searchQuery}), '%'))
                      AND\s
                        (TRIM(:#{#adStaffRequest.idFacility}) IS NULL
                         OR TRIM(:#{#adStaffRequest.idFacility}) = ''
                         OR f.id = TRIM(:#{#adStaffRequest.idFacility}))
                      AND\s
                        (TRIM(:#{#adStaffRequest.status}) IS NULL
                         OR TRIM(:#{#adStaffRequest.status}) = ''
                         OR s.status = TRIM(:#{#adStaffRequest.status}))

                                        """
    )
    Page<AdStaffRespone> getAllStaff(Pageable pageable, AdStaffRequest adStaffRequest);

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
    Optional<AdStaffDetailRespone> getDetailStaff(String id);

    Optional<UserStaff> findUserStaffByIdAndStatus(String staffId, EntityStatus status);
}
