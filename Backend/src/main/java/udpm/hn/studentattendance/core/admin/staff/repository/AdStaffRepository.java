package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.AdStaffDetailResponse;
import udpm.hn.studentattendance.core.admin.staff.model.response.AdStaffResponse;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;

@Repository
public interface AdStaffRepository extends UserStaffRepository {

    @Query("""
    SELECT 
         ROW_NUMBER() OVER (ORDER BY s.createdAt DESC) AS rowNumber,
         s.id AS staffId,
         s.name AS staffName,
         s.code AS staffCode,
         s.emailFe AS staffEmailFe,
         s.emailFpt AS staffEmailFpt,
         s.status AS staffStatus,
         f.name AS facilityName
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
      AND (:#{#adStaffRequest.status} IS NULL 
           OR :#{#adStaffRequest.status} = '' 
           OR s.status = :#{#adStaffRequest.status})
    GROUP BY s.id, s.name, s.code, s.emailFe, s.emailFpt, s.status, s.createdAt
    ORDER BY s.createdAt DESC
    """)
    Page<AdStaffResponse> getAllStaff(Pageable pageable, AdStaffRequest adStaffRequest);



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
    Optional<AdStaffDetailResponse> getDetailStaff(String id);

    Optional<UserStaff> findUserStaffByIdAndStatus(String staffId, EntityStatus status);
}
