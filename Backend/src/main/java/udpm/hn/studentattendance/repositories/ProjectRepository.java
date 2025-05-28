package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
}
