package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserActivityLog;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, String> {

}
