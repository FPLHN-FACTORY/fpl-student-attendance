package udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;

@Repository
public interface DBGeneratorUserStaffRepository extends UserStaffRepository {

    Optional<UserStaff> findByEmailFpt(String email);

}
