package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.Admin_CheckStaffRoleResponse;
import udpm.hn.studentattendance.core.admin.staff.model.response.Admin_StaffRoleResponse;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface Admin_StaffRoleRepository extends RoleRepository {

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
                            WHERE 
                            s.status = :status
                            AND 
                            r.status = :roleStatus
                            AND TRIM(s.id) = TRIM(:staffId)
                            """
    )
    List<Admin_StaffRoleResponse> getRolesByStaffId(String staffId, EntityStatus status, EntityStatus roleStatus);

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
                              AND s.status = :statusRole
                       )
                       THEN 'true'
                       ELSE 'false'
                   END as checked
            FROM Role r
            JOIN Facility f ON f.id = r.facility.id 
            WHERE r.status = :statusRole
              AND f.status = :statusFacility
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
             WHERE r.status = :statusRole
              AND f.status = :statusFacility
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
    Page<Admin_CheckStaffRoleResponse> getRolesChecked(Pageable pageable, Admin_StaffRoleRequest adStaffRoleRequest, EntityStatus statusRole, EntityStatus statusFacility);


    @Query("""
    SELECT 
    r
    FROM Role r
    LEFT JOIN UserStaff us on r.userStaff.id = us.id
    WHERE 
    r.code = :roleCode
    AND us.id= :staffId
    
""")
    List<Role> findAllByCodeAndUserStaffId(RoleConstant roleCode, String staffId);

    List<Role> findAllByUserStaffIdAndStatus(String staffId, EntityStatus status);

    List<Role> findAllByUserStaffId(String staffId);
}
