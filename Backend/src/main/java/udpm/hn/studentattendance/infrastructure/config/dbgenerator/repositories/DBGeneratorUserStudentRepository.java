package udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.Optional;

@Repository
public interface DBGeneratorUserStudentRepository extends UserStudentRepository {

    Optional<UserStudent> findByEmail(String email);

}
