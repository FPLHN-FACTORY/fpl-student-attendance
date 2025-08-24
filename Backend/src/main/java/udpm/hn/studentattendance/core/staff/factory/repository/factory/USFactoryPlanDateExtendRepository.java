package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;

@Repository
public interface USFactoryPlanDateExtendRepository extends PlanDateRepository {

    List<PlanDate> getPlanDatesByPlanFactoryId(String planFactoryId);
}
