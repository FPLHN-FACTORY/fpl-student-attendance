package udpm.hn.studentattendance.core.admin.role.repository;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface AdRoleStaffRepository extends RoleRepository {
    List<UserStaff> findAllByIdAndStatus( String roleId, EntityStatus entityStatus);
}
