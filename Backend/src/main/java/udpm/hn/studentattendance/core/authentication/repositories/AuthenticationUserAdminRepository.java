package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.authentication.model.response.AuthenticationInfoUserResponse;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.Optional;

@Repository
public interface AuthenticationUserAdminRepository extends UserAdminRepository {

    Optional<UserAdmin> findByEmail(String email);

}
