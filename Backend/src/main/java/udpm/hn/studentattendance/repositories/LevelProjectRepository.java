package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.LevelProject;

@Repository
public interface LevelProjectRepository extends JpaRepository<LevelProject, String> {
}
