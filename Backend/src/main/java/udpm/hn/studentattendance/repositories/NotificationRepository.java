package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
}
