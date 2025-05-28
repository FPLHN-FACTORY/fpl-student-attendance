package udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.Optional;

@Repository
public interface DBGeneratorUserAdminRepository extends UserAdminRepository {

    Optional<UserAdmin> findByEmail(String email);

}
