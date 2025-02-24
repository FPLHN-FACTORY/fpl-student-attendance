package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudentProject;

@Repository
public interface UserStudentProjectRepository extends JpaRepository<UserStudentProject, String> {
}
