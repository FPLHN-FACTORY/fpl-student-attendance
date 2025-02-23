package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.Optional;

@Repository
public interface AuthenticationUserStudentRepository extends UserStudentRepository {

    Optional<UserStudent> findByEmailAndFacility_Id(String email, String idFacility);

}
