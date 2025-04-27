package udpm.hn.studentattendance.core.admin.user_staff.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface ADStaffRoleRepository extends RoleRepository {
    List<Role> findAllByUserStaffId(String staffId);

    @Query("""
            SELECT
            r
            FROM Role r
            """)
    List<Role> getAllRole();
}
