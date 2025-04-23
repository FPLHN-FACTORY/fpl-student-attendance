package udpm.hn.studentattendance.core.admin.user_staff.repository;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.Optional;

@Repository
public interface Admin_StaffAdminRepository extends UserAdminRepository {

    Optional<UserAdmin> getUserAdminByCode(String code);
}
