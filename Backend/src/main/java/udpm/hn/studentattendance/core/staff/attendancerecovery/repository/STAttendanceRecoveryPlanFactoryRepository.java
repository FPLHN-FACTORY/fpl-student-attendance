package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.PlanFactoryRepository;

@Repository
public interface STAttendanceRecoveryPlanFactoryRepository extends PlanFactoryRepository {

    @Query("""
             SELECT
              pf
              FROM PlanFactory pf
              WHERE pf.factory.id = :factoryId
              AND pf.status = :status
              AND pf.factory.status = :factoryStatus
            """
    )
    PlanFactory getPlanFactoryByFactoryId(String factoryId, EntityStatus status, EntityStatus factoryStatus);
}
