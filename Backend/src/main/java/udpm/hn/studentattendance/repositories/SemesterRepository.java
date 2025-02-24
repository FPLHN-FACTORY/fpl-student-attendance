package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
}
