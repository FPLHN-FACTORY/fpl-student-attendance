package udpm.hn.studentattendance.core.admin.role.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.role.model.request.Admin_RoleRequest;
import udpm.hn.studentattendance.core.admin.role.model.response.Admin_RoleResponse;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface Admin_RoleRepository extends RoleRepository {

    @Query(value = """
            SELECT 
                   r.id,
                   str(r.code),
                   f.name
            FROM Role r
            LEFT JOIN r.facility f
            WHERE r.status = 0
              AND f.status = 0
              AND (:#{#adRoleRequest.roleCode} IS NULL 
                   OR :#{#adRoleRequest.roleCode} = '' 
                   OR str(r.code) LIKE CONCAT('%', TRIM(:#{#adRoleRequest.roleCode}), '%'))
              AND (:#{#adRoleRequest.idFacility} IS NULL 
                   OR :#{#adRoleRequest.idFacility} = '' 
                   OR str(r.facility.id) LIKE CONCAT('%', TRIM(:#{#adRoleRequest.idFacility}), '%'))
            """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM Role r
                    LEFT JOIN r.facility f
                    WHERE r.status = 0
                      AND f.status = 0
                      AND (:#{#adRoleRequest.roleCode} IS NULL 
                           OR :#{#adRoleRequest.roleCode} = '' 
                           OR str(r.code) LIKE CONCAT('%', TRIM(:#{#adRoleRequest.roleCode}), '%'))
                      AND (:#{#adRoleRequest.idFacility} IS NULL 
                           OR :#{#adRoleRequest.idFacility} = '' 
                           OR str(r.facility.id) LIKE CONCAT('%', TRIM(:#{#adRoleRequest.idFacility}), '%'))
                    """
    )
    Page<Admin_RoleResponse> getAllRole(Pageable pageable, Admin_RoleRequest adRoleRequest);

    List<Role> findAllByCode(RoleConstant code);

    List<Role> findAllByCodeAndStatus(RoleConstant code, EntityStatus status);

}
