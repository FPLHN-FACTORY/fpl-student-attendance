package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.Optional;

@Repository
public interface AdStaffAdminRepository extends UserAdminRepository {

    Optional<UserAdmin> getUserAdminByCode(String code);
}
