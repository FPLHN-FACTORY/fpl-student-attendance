package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.repositories.PlanRepository;
@Repository
public interface USFactoryProjectPlanExtendRepository extends PlanRepository {

    Plan getPlanByProjectId(String projectId);
    boolean existsByFactoryIdAndPlanId(String factoryId, String planId);

}
