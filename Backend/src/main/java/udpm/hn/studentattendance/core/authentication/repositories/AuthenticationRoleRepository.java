package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.repositories.RoleRepository;

import java.util.List;

@Repository
public interface AuthenticationRoleRepository extends RoleRepository {

    @Query(value = """
                SELECT * FROM role
                WHERE id_user_staff = :userId
            """, nativeQuery = true)
    List<Role> findRolesByUserId(String userId);

}
