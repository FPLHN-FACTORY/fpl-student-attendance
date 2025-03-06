package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.AdCheckStaffRoleResponse;
import udpm.hn.studentattendance.core.admin.staff.model.response.AdStaffRoleResponse;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface AdStaffRoleRepository extends RoleRepository {

    @Query(
            value =
                    """
                            SELECT
                                r.id as roleId,
                                r.code as roleCode,
                                f.name as facilityName              
                            FROM UserStaff s  
                            LEFT JOIN Role r ON s.id = r.userStaff.id
                            LEFT JOIN Facility f ON r.facility.id = f.id
                            WHERE s.status = 0 
                              AND TRIM(s.id) = TRIM(:staffId)
                            """
    )
    List<AdStaffRoleResponse> getRolesByStaffId(String staffId);

    @Query(
            value = """
            SELECT 
                   r.id AS idRole,
                   r.code AS roleCode,
                   f.name AS facilityName,
                   CASE
                       WHEN r.id IN (
                            SELECT role.id 
                            FROM UserStaff s JOIN s.roles role
                            WHERE s.id = :#{#adStaffRoleRequest.staffId} 
                              AND s.status = 0
                       )
                       THEN 'true'
                       ELSE 'false'
                   END as checked
            FROM Role r
            JOIN Facility f ON f.id = r.facility.id 
            WHERE r.status = 0 
              AND f.status = 0
              AND (
                    :#{#adStaffRoleRequest.roleCode} IS NULL
                 OR :#{#adStaffRoleRequest.roleCode} = ''
                 OR cast(r.code as string) LIKE CONCAT('%', :#{#adStaffRoleRequest.roleCode}, '%')
              )
              AND (
                    :#{#adStaffRoleRequest.idFacility} IS NULL
                 OR TRIM(:#{#adStaffRoleRequest.idFacility}) = ''
                 OR f.id = TRIM(:#{#adStaffRoleRequest.idFacility})
              )
            ORDER BY r.updatedAt DESC
            """,
            countQuery = """
             SELECT COUNT(r)
             FROM Role r
             JOIN Facility f ON f.id = r.facility.id 
             WHERE r.status = 0 
               AND f.status = 0
               AND (
                    :#{#adStaffRoleRequest.roleCode} IS NULL
                 OR :#{#adStaffRoleRequest.roleCode} = ''
                 OR cast(r.code as string) LIKE CONCAT('%', :#{#adStaffRoleRequest.roleCode}, '%')
               )
               AND (
                    :#{#adStaffRoleRequest.idFacility} IS NULL
                 OR TRIM(:#{#adStaffRoleRequest.idFacility}) = ''
                 OR f.id = TRIM(:#{#adStaffRoleRequest.idFacility})
               )
             ORDER BY r.updatedAt DESC
            """
    )
    Page<AdCheckStaffRoleResponse> getRolesChecked(Pageable pageable, AdStaffRoleRequest adStaffRoleRequest);


    List<Role> findAllByIdAndUserStaffId(String roleId, String staffId);

    List<Role> findAllByUserStaffIdAndStatus(String staffId, EntityStatus status);

    List<Role> findAllByUserStaffId(String staffId);
}
