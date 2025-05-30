package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.repositories.PlanRepository;

@Repository
public interface USFactoryProjectPlanExtendRepository extends PlanRepository {

    Plan getPlanByProjectId(String projectId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE  a, pd, pf
            FROM plan p
            LEFT JOIN plan_factory pf ON pf.id_plan = p.id
            LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
            LEFT JOIN attendance a ON a.id_plan_date = pd.id
            WHERE p.id = :planId
            """, nativeQuery = true)
    void deleteAllAttendanceAndPlanDateAndPlanFactoryByPlan(String planId);
}
