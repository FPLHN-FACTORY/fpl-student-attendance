package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudent;

@Repository
public interface UserStudentRepository extends JpaRepository<UserStudent, String> {
}
