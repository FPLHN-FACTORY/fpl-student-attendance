package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.PlanDate;

@Repository
public interface PlanDateRepository extends JpaRepository<PlanDate, String> {
    // Basic CRUD operations are inherited from JpaRepository
    // Specific queries have been moved to ScheduleReminderRepository
}
