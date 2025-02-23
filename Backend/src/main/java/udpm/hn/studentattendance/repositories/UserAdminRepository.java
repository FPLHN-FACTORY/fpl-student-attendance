package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserAdmin;

@Repository
public interface UserAdminRepository extends JpaRepository<UserAdmin, String> {
}
