package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;

@Repository
public interface UserStaffRepository extends JpaRepository<UserStaff, String> {
}
