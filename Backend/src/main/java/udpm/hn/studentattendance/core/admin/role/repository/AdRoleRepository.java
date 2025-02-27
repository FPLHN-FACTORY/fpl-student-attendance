package udpm.hn.studentattendance.core.admin.role.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.role.model.request.AdRoleRequest;
import udpm.hn.studentattendance.core.admin.role.model.response.AdRoleResponse;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface AdRoleRepository extends RoleRepository {

    @Query(value = """
            SELECT new udpm.hn.studentattendance.core.admin.role.model.response.AdRoleResponse(
                   r.id,
                   str(r.code),
                   f.name
            )
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
    Page<AdRoleResponse> getAllRole(Pageable pageable, AdRoleRequest adRoleRequest);

    List<Role> findAllByCode(RoleConstant code);

    List<Role> findAllByCodeAndStatus(RoleConstant code, EntityStatus status);

}
