package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.entities.PlanFactory;

@Repository
public interface ImportLogRepository extends JpaRepository<ImportLog, String> {

}
